package com.example.visaslotbot.service;

import com.example.visaslotbot.model.SlotEvent;
import com.example.visaslotbot.model.SlotSnapshot;
import com.example.visaslotbot.model.Subscription;
import com.example.visaslotbot.model.VisaCenter;
import com.example.visaslotbot.repository.SlotEventRepository;
import com.example.visaslotbot.repository.SlotSnapshotRepository;
import com.example.visaslotbot.scraper.SlotData;
import com.example.visaslotbot.scraper.SlotScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotCheckService {

    private final SlotScraper slotScraper;
    private final SlotSnapshotRepository slotSnapshotRepository;
    private final SlotEventRepository slotEventRepository;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    public void checkCenter(VisaCenter center) {
        SlotData slotData = slotScraper.fetchSlots(center);
        String newHash = slotData.hash();

        SlotSnapshot lastSnapshot = slotSnapshotRepository.findTopByVisaCenterOrderBySnapshotTimeDesc(center).orElse(null);
        if (lastSnapshot != null && newHash.equals(lastSnapshot.getRawDataHash())) {
            log.debug("No new data for center {}", center.getName());
            return;
        }

        SlotSnapshot snapshot = SlotSnapshot.builder()
                .visaCenter(center)
                .snapshotTime(Instant.now())
                .rawDataHash(newHash)
                .summary(slotData.toSummaryString())
                .build();
        slotSnapshotRepository.save(snapshot);

        SlotEvent event = SlotEvent.builder()
                .visaCenter(center)
                .eventTime(Instant.now())
                .description(slotData.toHumanReadableString())
                .build();
        slotEventRepository.save(event);

        notifySubscribers(center, event, slotData);
    }

    private void notifySubscribers(VisaCenter center, SlotEvent event, SlotData slotData) {
        List<Subscription> subscriptions = subscriptionService.activeSubscriptionsForCenter(center);
        if (subscriptions.isEmpty()) {
            log.info("No active subscriptions for center {}", center.getName());
            return;
        }
        String message = "✈️ Появились новые слоты!\n" +
                "Страна: " + center.getCountry() + "\n" +
                "Город: " + center.getCity() + "\n" +
                "Описание: " + slotData.toHumanReadableString() + "\n\n" +
                "Зайди на сайт визового центра и попробуй забронировать место.";
        subscriptions.forEach(sub -> notificationService.notifyUser(sub.getUser(), event, message));
    }
}
