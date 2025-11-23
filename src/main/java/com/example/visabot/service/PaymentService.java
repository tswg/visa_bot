package com.example.visabot.service;

import com.example.visabot.entity.Payment;
import com.example.visabot.entity.PaymentMethod;
import com.example.visabot.entity.PaymentStatus;
import com.example.visabot.entity.SubscriptionPlan;
import com.example.visabot.entity.User;
import com.example.visabot.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    public Payment createPayment(User user,
                                 SubscriptionPlan plan,
                                 PaymentMethod method,
                                 BigDecimal amount,
                                 String currency) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPlan(plan);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        switch (method) {
            case CARD, SBP -> {
                payment.setPaymentLink("https://pay.example.com/" + UUID.randomUUID());
                payment.setDescription("Оплата картой/СБП. Тестовый платеж.");
            }
            case CRYPTO -> payment.setDescription("Отправьте сумму " + amount + " " + currency
                    + " на кошелёк: YOUR_WALLET_ADDRESS_HERE (заглушка).");
            default -> throw new IllegalArgumentException("Неизвестный способ оплаты: " + method);
        }

        return paymentRepository.save(payment);
    }

    public void markPaymentSuccessful(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Платёж не найден: " + paymentId));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        subscriptionService.upgradeUserSubscriptionsToPremium(payment.getUser());
    }
}
