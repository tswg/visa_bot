package com.example.visabot.notification;

import com.example.visabot.entity.Notification;
import com.example.visabot.entity.NotificationStatus;
import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.NotificationRepository;
import com.example.visabot.repository.SubscriptionRepository;
import com.example.visabot.telegram.TelegramBot;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final TelegramBot telegramBot;

    public void notifySubscribers(VisaCenter center, SlotEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
                .findByVisaCenterAndStatusAndValidToAfter(center, SubscriptionStatus.ACTIVE, LocalDateTime.now());
        for (Subscription subscription : subscriptions) {
            sendNotification(subscription, event);
        }
    }

    private void sendNotification(Subscription subscription, SlotEvent event) {
        Notification notification = new Notification();
        notification.setUser(subscription.getUser());
        notification.setSubscription(subscription);
        notification.setSlotEvent(event);
        String message = String.format("Появились новые слоты для %s / %s", event.getVisaCenter().getCountry(),
                event.getVisaCenter().getCity());
        notification.setMessage(message);
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(subscription.getUser().getTelegramId().toString())
                    .text(message)
                    .build();
            telegramBot.execute(sendMessage);
            notification.setStatus(NotificationStatus.SENT);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification to user {}", subscription.getUser().getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }
}
