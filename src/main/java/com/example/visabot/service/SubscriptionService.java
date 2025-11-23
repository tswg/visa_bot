package com.example.visabot.service;

import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionPlan;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.repository.SubscriptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public void upgradeUserSubscriptionsToPremium(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByUserAndStatusAndValidToAfter(user, SubscriptionStatus.ACTIVE, now);

        boolean hasChanges = false;
        for (Subscription subscription : activeSubscriptions) {
            if (subscription.getPlan() != SubscriptionPlan.PREMIUM) {
                subscription.setPlan(SubscriptionPlan.PREMIUM);
                hasChanges = true;
            }
        }
        if (hasChanges) {
            subscriptionRepository.saveAll(activeSubscriptions);
        }
    }
}
