package com.example.visaslotbot.service;

import com.example.visaslotbot.bot.VisaSlotTelegramBot;
import com.example.visaslotbot.model.Notification;
import com.example.visaslotbot.model.NotificationStatus;
import com.example.visaslotbot.model.SlotEvent;
import com.example.visaslotbot.model.User;
import com.example.visaslotbot.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final VisaSlotTelegramBot telegramBot;
    private final UserService userService;

    public void notifyUser(User user, SlotEvent event, String message) {
        Notification.NotificationBuilder builder = Notification.builder()
                .user(user)
                .slotEvent(event)
                .sentAt(Instant.now());
        try {
            telegramBot.sendNotification(user.getTelegramId(), message);
            builder.status(NotificationStatus.SENT);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification to user {}", user.getTelegramId(), e);
            builder.status(NotificationStatus.FAILED);
            userService.markBlocked(user);
        }
        notificationRepository.save(builder.build());
    }
}
