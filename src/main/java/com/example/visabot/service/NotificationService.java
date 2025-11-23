package com.example.visabot.service;

import com.example.visabot.entity.Notification;
import com.example.visabot.entity.NotificationStatus;
import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionPlan;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.repository.NotificationRepository;
import com.example.visabot.repository.SubscriptionRepository;
import com.example.visabot.repository.UserRepository;
import com.example.visabot.telegram.TelegramBot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;

    public void notifySubscribersAboutNewSlots(VisaCenter center, SlotEvent event) {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> premiumSubscriptions = subscriptionRepository
                .findActiveByVisaCenterWithUserAndPlan(center, SubscriptionStatus.ACTIVE, SubscriptionPlan.PREMIUM, now);

        List<Subscription> basicSubscriptions = subscriptionRepository
                .findActiveByVisaCenterWithUserAndPlan(center, SubscriptionStatus.ACTIVE, SubscriptionPlan.BASIC, now);

        for (Subscription subscription : premiumSubscriptions) {
            sendNotification(subscription, event);
        }

        for (Subscription subscription : basicSubscriptions) {
            sendNotification(subscription, event);
        }
    }

    private void sendNotification(Subscription subscription, SlotEvent event) {
        User user = resolveUser(subscription);
        if (user == null || user.getTelegramId() == null) {
            log.warn("Skipping notification for subscription {} because user or telegramId is missing", subscription.getId());
            return;
        }

        Boolean notificationsEnabled = user.getNotificationsEnabled();
        if (notificationsEnabled != null && !notificationsEnabled) {
            log.info("Skipping notification for user {} due to notification settings", user.getId());
            return;
        }

        if (Boolean.TRUE.equals(user.getDndNightEnabled()) && isNightNow()) {
            log.info("Skipping notification for user {} due to notification settings", user.getId());
            return;
        }

        LocalDate userFromDate = user.getNotifyFromDate();
        LocalDate eventEarliest = event.getEarliestDate();
        if (userFromDate != null && eventEarliest != null && eventEarliest.isBefore(userFromDate)) {
            log.info("Skipping notification for user {} due to notifyFromDate {} (earliest event date: {})",
                    user.getId(), userFromDate, eventEarliest);
            return;
        }

        String message = buildMessage(event);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSubscription(subscription);
        notification.setSlotEvent(event);
        notification.setMessage(message);

        telegramBot.sendMessage(user.getTelegramId(), message);
        notification.setStatus(NotificationStatus.SENT);
        log.info("Notification sent to user {} for center {} with plan {}", user.getId(),
                event.getVisaCenter().getId(), subscription.getPlan());

        notificationRepository.save(notification);
    }

    private boolean isNightNow() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(8, 0));
    }

    private User resolveUser(Subscription subscription) {
        User subscriptionUser = subscription.getUser();
        if (subscriptionUser == null) {
            return null;
        }
        return userRepository.findById(subscriptionUser.getId()).orElse(subscriptionUser);
    }

    private String buildMessage(SlotEvent event) {
        VisaCenter center = event.getVisaCenter();
        String centerInfo = String.format("%s / %s — %s", center.getCountry(), center.getCity(), center.getName());

        String description = Optional.ofNullable(event.getDescription())
                .filter(StringUtils::hasText)
                .or(() -> Optional.ofNullable(event.getSnapshot())
                        .map(SlotSnapshot::getRawData)
                        .filter(StringUtils::hasText))
                .orElse("Найдены новые свободные слоты.");

        return "✈️ Появились новые слоты!\n\n" +
                "Центр: " + centerInfo + "\n" +
                "Описание: " + description + "\n\n" +
                "Зайдите на сайт визового центра и попробуйте забронировать место как можно скорее.";
    }
}
