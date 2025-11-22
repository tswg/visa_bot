package com.example.visaslotbot.service;

import com.example.visaslotbot.model.Subscription;
import com.example.visaslotbot.model.SubscriptionStatus;
import com.example.visaslotbot.model.User;
import com.example.visaslotbot.model.VisaCenter;
import com.example.visaslotbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Subscription subscribe(User user, VisaCenter center) {
        Instant validTo = Instant.now().plus(7, ChronoUnit.DAYS);
        Subscription subscription = subscriptionRepository.findTopByUserAndVisaCenterOrderByCreatedAtDesc(user, center)
                .orElseGet(() -> Subscription.builder()
                        .user(user)
                        .visaCenter(center)
                        .createdAt(Instant.now())
                        .build());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setValidTo(validTo);
        subscription.setCreatedAt(subscription.getCreatedAt() == null ? Instant.now() : subscription.getCreatedAt());
        return subscriptionRepository.save(subscription);
    }

    public Optional<Subscription> latestSubscription(User user, VisaCenter center) {
        return subscriptionRepository.findTopByUserAndVisaCenterOrderByCreatedAtDesc(user, center);
    }

    public List<Subscription> activeSubscriptionsForCenter(VisaCenter center) {
        return subscriptionRepository.findByVisaCenterAndStatusAndValidToAfter(center, SubscriptionStatus.ACTIVE, Instant.now());
    }
}
