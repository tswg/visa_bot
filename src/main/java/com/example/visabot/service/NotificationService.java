package com.example.visabot.service;

import com.example.visabot.entity.Notification;
import com.example.visabot.entity.NotificationStatus;
import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.NotificationRepository;
import com.example.visabot.repository.SubscriptionRepository;
import com.example.visabot.telegram.TelegramBot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final TelegramBot telegramBot;

    public void notifySubscribersAboutNewSlots(VisaCenter center, SlotEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
                .findByVisaCenterAndStatusAndValidToAfter(center, SubscriptionStatus.ACTIVE, LocalDateTime.now());

        for (Subscription subscription : subscriptions) {
            sendNotification(subscription, event);
        }
    }

    private void sendNotification(Subscription subscription, SlotEvent event) {
        User user = subscription.getUser();
        if (user == null || user.getTelegramId() == null) {
            log.warn("Skipping notification for subscription {} because user or telegramId is missing", subscription.getId());
            return;
        }

        String message = buildMessage(event);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSubscription(subscription);
        notification.setSlotEvent(event);
        notification.setMessage(message);

        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId().toString())
                    .text(message)
                    .build();
            telegramBot.execute(sendMessage);
            notification.setStatus(NotificationStatus.SENT);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification to user {}", user.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    private String buildMessage(SlotEvent event) {
        VisaCenter center = event.getVisaCenter();
        String centerInfo = String.format("%s / %s — %s", center.getCountry(), center.getCity(), center.getName());

        String description = Optional.ofNullable(event.getSnapshot())
                .map(SlotSnapshot::getRawData)
                .filter(StringUtils::hasText)
                .orElse("Найдены новые свободные слоты.");

        return "✈️ Появились новые слоты!\n\n" +
                "Центр: " + centerInfo + "\n" +
                "Описание: " + description + "\n\n" +
                "Зайдите на сайт визового центра и попробуйте забронировать место как можно скорее.";
    }
}
