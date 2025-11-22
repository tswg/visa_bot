package com.example.visaslotbot.bot;

import com.example.visaslotbot.model.Subscription;
import com.example.visaslotbot.model.User;
import com.example.visaslotbot.model.VisaCenter;
import com.example.visaslotbot.service.SubscriptionService;
import com.example.visaslotbot.service.UserService;
import com.example.visaslotbot.service.VisaCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class VisaSlotTelegramBot extends TelegramLongPollingBot {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final VisaCenterService visaCenterService;

    @Value("${telegram.bot.username:visa_slot_bot}")
    private String botUsername;

    @Value("${telegram.bot.token:dummy}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText().trim();
        Long chatId = message.getChatId();
        User user = userService.getOrCreateUser(chatId, message.getFrom() != null ? message.getFrom().getUserName() : null);

        switch (text) {
            case "/start" -> handleStart(chatId);
            case "/subscribe" -> handleSubscribe(user, chatId);
            case "/status" -> handleStatus(user, chatId);
            default -> sendText(chatId, "Неизвестная команда. Используйте /subscribe или /status.");
        }
    }

    private void handleStart(Long chatId) {
        String intro = "Привет! Этот бот присылает уведомления, когда появляются новые слоты в визовом центре.\n"
                + "Используй /subscribe, чтобы подписаться на тестовый центр, и /status, чтобы проверить подписку.";
        sendText(chatId, intro);
    }

    private void handleSubscribe(User user, Long chatId) {
        VisaCenter center = visaCenterService.findActiveTestCenter()
                .orElse(null);
        if (center == null) {
            sendText(chatId, "Нет активных визовых центров для подписки.");
            return;
        }
        Subscription subscription = subscriptionService.subscribe(user, center);
        sendText(chatId, "Подписка оформлена до " + DateTimeFormatter.ISO_INSTANT.format(subscription.getValidTo()));
    }

    private void handleStatus(User user, Long chatId) {
        VisaCenter center = visaCenterService.findActiveTestCenter().orElse(null);
        if (center == null) {
            sendText(chatId, "Нет доступных центров.");
            return;
        }
        subscriptionService.latestSubscription(user, center)
                .ifPresentOrElse(sub -> sendText(chatId, formatStatus(sub)),
                        () -> sendText(chatId, "У вас пока нет подписки. Используйте /subscribe."));
    }

    private String formatStatus(Subscription subscription) {
        return "Статус: " + subscription.getStatus() + " до " + DateTimeFormatter.ISO_INSTANT.format(subscription.getValidTo());
    }

    public void sendText(Long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}", chatId, e);
        }
    }

    public void sendNotification(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
