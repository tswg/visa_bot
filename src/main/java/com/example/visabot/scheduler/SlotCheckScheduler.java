package com.example.visabot.scheduler;

import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.SlotEventRepository;
import com.example.visabot.repository.SlotSnapshotRepository;
import com.example.visabot.repository.VisaCenterRepository;
import com.example.visabot.scraper.SlotScraperFactory;
import com.example.visabot.service.NotificationService;
import com.example.visabot.service.SlotData;
import com.example.visabot.service.SlotScraper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlotCheckScheduler {

    private final VisaCenterRepository visaCenterRepository;
    private final SlotScraperFactory slotScraperFactory;
    private final SlotSnapshotRepository slotSnapshotRepository;
    private final SlotEventRepository slotEventRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "120000")
    public void checkSlotsForAllCenters() {
        List<VisaCenter> centers = visaCenterRepository.findByActiveTrue();
        for (VisaCenter center : centers) {
            try {
                SlotScraper scraper = slotScraperFactory.getScraperFor(center);
                SlotData current = scraper.fetchSlots(center);
                processSlotData(center, current);
            } catch (Exception e) {
                log.error("Error checking center {}", center.getId(), e);
            }
        }
    }

    public void processSlotData(VisaCenter center, SlotData slotData) {
        SlotSnapshot lastSnapshot = slotSnapshotRepository
                .findTopByVisaCenterOrderByCreatedAtDesc(center)
                .orElse(null);
        if (lastSnapshot != null && slotData.hash().equals(lastSnapshot.getRawDataHash())) {
            return;
        }

        SlotSnapshot newSnapshot = new SlotSnapshot();
        newSnapshot.setVisaCenter(center);
        newSnapshot.setRawData(slotData.toHumanReadableString());
        newSnapshot.setRawDataHash(slotData.hash());
        slotSnapshotRepository.save(newSnapshot);

        SlotEvent event = new SlotEvent();
        event.setVisaCenter(center);
        event.setSnapshot(newSnapshot);
        event.setDescription(slotData.toSummaryString());
        slotEventRepository.save(event);

        notificationService.notifySubscribersAboutNewSlots(center, event);
    }
}
