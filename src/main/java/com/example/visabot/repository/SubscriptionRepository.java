package com.example.visabot.repository;

import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByVisaCenterAndStatusAndValidToAfter(VisaCenter visaCenter, SubscriptionStatus status, LocalDateTime now);

    Optional<Subscription> findByUserAndVisaCenter(User user, VisaCenter visaCenter);
}
