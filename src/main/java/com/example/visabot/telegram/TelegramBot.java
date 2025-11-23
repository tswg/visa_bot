package com.example.visabot.telegram;

import com.example.visabot.config.BotProperties;
import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionPlan;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.SubscriptionRepository;
import com.example.visabot.repository.UserRepository;
import com.example.visabot.repository.VisaCenterRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (update.getMessage() == null || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom() != null ? update.getMessage().getFrom().getUserName() : null;

        if (text.startsWith("/subscribe")) {
            handleSubscribe(chatId, text);
        } else {
            switch (text) {
                case "/start" -> handleStart(chatId, username);
                case "/status", "📝 Мои подписки" -> handleStatus(chatId);
                case "/centers", "📍 Визовые центры" -> handleCenters(chatId);
                case "/premium", "⭐ PREMIUM" -> handlePremium(chatId);
                default -> handleUnknown(chatId);
            }
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
                + "Команды: /centers — список доступных центров, /subscribe — подписка, /status — статус подписки, /premium — приоритетные уведомления.\n"
                + "Можно подписаться на номер центра через /subscribe <номер>.";
        sendMessage(chatId, welcome, createMainMenuKeyboard());
    }

    private void handleSubscribe(Long chatId, String text) {
        Optional<User> userOpt = userRepository.findByTelegramId(chatId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "Пожалуйста, сначала отправьте /start");
            return;
        }
        User user = userOpt.get();

        String[] parts = text.split("\\s+");
        if (parts.length == 1) {
            handleDefaultSubscription(chatId, user);
            return;
        }

        int index;
        try {
            index = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Некорректный номер центра. Сначала посмотри список через /centers.");
            return;
        }

        List<VisaCenter> centers = visaCenterRepository.findByActiveTrue();
        if (index < 1 || index > centers.size()) {
            sendMessage(chatId, "Некорректный номер центра. Сначала посмотри список через /centers.");
            return;
        }

        VisaCenter center = centers.get(index - 1);
        Subscription subscription = upsertSubscription(user, center);

        sendMessage(chatId, "Подписка на центр "
                + center.getCountry() + " / " + center.getCity() + " — " + center.getName()
                + " активна до " + subscription.getValidTo().format(DATE_FORMATTER));
    }

    private void handleDefaultSubscription(Long chatId, User user) {
        Optional<VisaCenter> centerOpt = visaCenterRepository
                .findByCountryAndCityAndName("Finland", "Helsinki", "Test center");
        if (centerOpt.isEmpty()) {
            sendMessage(chatId, "Тестовый визовый центр не найден");
            return;
        }
        VisaCenter center = centerOpt.get();
        Subscription subscription = upsertSubscription(user, center);

        sendMessage(chatId, "Подписка на центр "
                + center.getCountry() + " / " + center.getCity() + " — " + center.getName()
                + " активна до " + subscription.getValidTo().format(DATE_FORMATTER));
    }

    private Subscription upsertSubscription(User user, VisaCenter center) {
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
        if (subscription.getPlan() == null) {
            subscription.setPlan(SubscriptionPlan.BASIC);
        }
        return subscriptionRepository.save(subscription);
    }

    private void handleStatus(Long chatId) {
        Optional<User> userOpt = userRepository.findByTelegramId(chatId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "Пожалуйста, сначала отправьте /start");
            return;
        }
        User user = userOpt.get();
        List<Subscription> subscriptions = subscriptionRepository
                .findActiveWithVisaCenter(user, SubscriptionStatus.ACTIVE, LocalDateTime.now());

        if (subscriptions.isEmpty()) {
            sendMessage(chatId, "Активных подписок нет.");
            return;
        }

        StringBuilder builder = new StringBuilder("Ваши активные подписки:\n\n");
        subscriptions.forEach(subscription -> builder.append("- ")
                .append(subscription.getVisaCenter().getCountry()).append(" / ")
                .append(subscription.getVisaCenter().getCity()).append(" — ")
                .append(subscription.getVisaCenter().getName()).append(", до ")
                .append(subscription.getValidTo().format(DATE_FORMATTER))
                .append("\n"));
        sendMessage(chatId, builder.toString().trim());
    }

    private void handlePremium(Long chatId) {
        Optional<User> userOpt = userRepository.findByTelegramId(chatId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "Пожалуйста, сначала отправьте /start");
            return;
        }

        User user = userOpt.get();
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserAndStatusAndValidToAfter(user, SubscriptionStatus.ACTIVE, LocalDateTime.now());

        if (subscriptions.isEmpty()) {
            sendMessage(chatId, "У вас нет активных подписок для обновления.");
            return;
        }

        subscriptions.forEach(subscription -> subscription.setPlan(SubscriptionPlan.PREMIUM));
        subscriptionRepository.saveAll(subscriptions);

        sendMessage(chatId, "Ваши активные подписки переведены на PREMIUM. Теперь уведомления будут приходить раньше обычных пользователей.");
    }

    private void handleCenters(Long chatId) {
        List<VisaCenter> centers = visaCenterRepository.findByActiveTrue();
        if (centers.isEmpty()) {
            sendMessage(chatId, "Нет доступных визовых центров.");
            return;
        }

        StringBuilder builder = new StringBuilder("Доступные визовые центры:\n\n");
        for (int i = 0; i < centers.size(); i++) {
            VisaCenter center = centers.get(i);
            builder.append(i + 1).append(") ")
                    .append(center.getCountry()).append(" / ")
                    .append(center.getCity()).append(" — ")
                    .append(center.getName()).append("\n");
        }
        builder.append("\nЧтобы подписаться, отправь команду: /subscribe <номер>\n")
                .append("Пример: /subscribe 1");

        InlineKeyboardMarkup keyboardMarkup = buildCentersKeyboard(centers);
        sendMessage(chatId, builder.toString(), keyboardMarkup);
    }

    private void handleUnknown(Long chatId) {
        sendMessage(chatId, "Неизвестная команда. Доступные: /start, /subscribe, /status, /centers, /premium");
    }

    public void sendMessage(Long chatId, String text) {
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

    public void sendMessage(Long chatId, String text, ReplyKeyboard replyMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }

    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        KeyboardRow centersRow = new KeyboardRow();
        centersRow.add(new KeyboardButton("📍 Визовые центры"));

        KeyboardRow subscriptionsRow = new KeyboardRow();
        subscriptionsRow.add(new KeyboardButton("📝 Мои подписки"));

        KeyboardRow premiumRow = new KeyboardRow();
        premiumRow.add(new KeyboardButton("⭐ PREMIUM"));

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setKeyboard(List.of(centersRow, subscriptionsRow, premiumRow));
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup buildCentersKeyboard(List<VisaCenter> centers) {
        List<List<InlineKeyboardButton>> rows = centers.stream()
                .map(center -> {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(center.getCountry() + " / " + center.getCity() + " — " + center.getName());
                    button.setCallbackData("subscribe:" + center.getId());
                    return List.of(button);
                })
                .toList();
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        if (callbackData == null) {
            return;
        }

        if (callbackData.startsWith("subscribe:")) {
            handleSubscribeCallback(callbackQuery, callbackData.substring("subscribe:".length()));
        }
    }

    private void handleSubscribeCallback(CallbackQuery callbackQuery, String centerIdStr) {
        Long centerId;
        try {
            centerId = Long.parseLong(centerIdStr);
        } catch (NumberFormatException e) {
            answerCallback(callbackQuery, "Некорректный идентификатор центра");
            return;
        }

        Optional<VisaCenter> centerOpt = visaCenterRepository.findById(centerId);
        if (centerOpt.isEmpty()) {
            answerCallback(callbackQuery, "Визовый центр не найден");
            return;
        }
        VisaCenter center = centerOpt.get();

        Long telegramId = callbackQuery.getFrom().getId();
        String username = callbackQuery.getFrom().getUserName();
        User user = userRepository.findByTelegramId(telegramId).orElseGet(() -> {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setUsername(username);
            return userRepository.save(newUser);
        });

        Subscription subscription = upsertSubscription(user, center);
        answerCallback(callbackQuery, "Подписка обновлена");

        Long chatId = callbackQuery.getMessage().getChatId();
        sendMessage(chatId, "Подписка на центр "
                + center.getCountry() + " / " + center.getCity() + " — " + center.getName()
                + " активна до " + subscription.getValidTo().format(DATE_FORMATTER));
    }

    private void answerCallback(CallbackQuery callbackQuery, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .text(text)
                .build();
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback {}", callbackQuery.getId(), e);
        }
    }
}
