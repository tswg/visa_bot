package com.example.visabot.scheduler;

import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.notification.NotificationService;
import com.example.visabot.repository.SlotEventRepository;
import com.example.visabot.repository.SlotSnapshotRepository;
import com.example.visabot.repository.VisaCenterRepository;
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
    private final SlotScraper slotScraper;
    private final SlotSnapshotRepository slotSnapshotRepository;
    private final SlotEventRepository slotEventRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "120000")
    public void checkSlotsForAllCenters() {
        List<VisaCenter> centers = visaCenterRepository.findByActiveTrue();
        for (VisaCenter center : centers) {
            try {
                SlotData current = slotScraper.fetchSlots(center);
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
        if (lastSnapshot != null && slotData.getRawDataHash().equals(lastSnapshot.getRawDataHash())) {
            return;
        }

        SlotSnapshot newSnapshot = new SlotSnapshot();
        newSnapshot.setVisaCenter(center);
        newSnapshot.setRawData(slotData.getRawData());
        newSnapshot.setRawDataHash(slotData.getRawDataHash());
        slotSnapshotRepository.save(newSnapshot);

        SlotEvent event = new SlotEvent();
        event.setVisaCenter(center);
        event.setSnapshot(newSnapshot);
        slotEventRepository.save(event);

        notificationService.notifySubscribers(center, event);
    }
}
