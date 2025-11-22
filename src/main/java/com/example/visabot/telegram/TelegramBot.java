package com.example.visabot.telegram;

import com.example.visabot.config.BotProperties;
import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.SubscriptionRepository;
import com.example.visabot.repository.UserRepository;
import com.example.visabot.repository.VisaCenterRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final BotProperties botProperties;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VisaCenterRepository visaCenterRepository;

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom() != null ? update.getMessage().getFrom().getUserName() : null;

        switch (text) {
            case "/start" -> handleStart(chatId, username);
            case "/subscribe" -> handleSubscribe(chatId);
            case "/status" -> handleStatus(chatId);
            default -> handleUnknown(chatId);
        }
    }

    private void handleStart(Long chatId, String username) {
        userRepository.findByTelegramId(chatId).orElseGet(() -> {
            User user = new User();
            user.setTelegramId(chatId);
            user.setUsername(username);
            return userRepository.save(user);
        });

        String welcome = "Привет! Я бот, который уведомляет, когда появляются свободные слоты в визовых центрах.\n"
                + "Доступен тестовый центр: Finland / Helsinki.\n"
                + "Команды: /subscribe — подписка, /status — статус подписки.";
        sendMessage(chatId, welcome);
    }

    private void handleSubscribe(Long chatId) {
        Optional<User> userOpt = userRepository.findByTelegramId(chatId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "Пожалуйста, сначала отправьте /start");
            return;
        }
        User user = userOpt.get();
        Optional<VisaCenter> centerOpt = visaCenterRepository
                .findByCountryAndCityAndName("Finland", "Helsinki", "Test center");
        if (centerOpt.isEmpty()) {
            sendMessage(chatId, "Тестовый визовый центр не найден");
            return;
        }
        VisaCenter center = centerOpt.get();
        Subscription subscription = subscriptionRepository
                .findByUserAndVisaCenter(user, center)
                .orElseGet(() -> {
                    Subscription sub = new Subscription();
                    sub.setUser(user);
                    sub.setVisaCenter(center);
                    return sub;
                });
        subscription.setValidTo(LocalDateTime.now().plusDays(7));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        sendMessage(chatId,
                "Подписка активна до " + subscription.getValidTo().format(DATE_FORMATTER));
    }

    private void handleStatus(Long chatId) {
        Optional<User> userOpt = userRepository.findByTelegramId(chatId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "Пожалуйста, сначала отправьте /start");
            return;
        }
        User user = userOpt.get();
        Optional<VisaCenter> centerOpt = visaCenterRepository
                .findByCountryAndCityAndName("Finland", "Helsinki", "Test center");
        if (centerOpt.isEmpty()) {
            sendMessage(chatId, "Тестовый визовый центр не найден");
            return;
        }
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUserAndVisaCenter(user, centerOpt.get());
        if (subscriptionOpt.isEmpty() || subscriptionOpt.get().getValidTo().isBefore(LocalDateTime.now())
                || subscriptionOpt.get().getStatus() != SubscriptionStatus.ACTIVE) {
            sendMessage(chatId, "Активной подписки нет");
            return;
        }
        Subscription subscription = subscriptionOpt.get();
        sendMessage(chatId, "Подписка активна до " + subscription.getValidTo().format(DATE_FORMATTER));
    }

    private void handleUnknown(Long chatId) {
        sendMessage(chatId, "Неизвестная команда. Доступные: /start, /subscribe, /status");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }
}
