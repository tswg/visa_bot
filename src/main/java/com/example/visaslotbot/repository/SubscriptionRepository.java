package com.example.visaslotbot.repository;

import com.example.visaslotbot.model.Subscription;
import com.example.visaslotbot.model.SubscriptionStatus;
import com.example.visaslotbot.model.User;
import com.example.visaslotbot.model.VisaCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserAndVisaCenterOrderByCreatedAtDesc(User user, VisaCenter visaCenter);

    List<Subscription> findByVisaCenterAndStatusAndValidToAfter(VisaCenter visaCenter, SubscriptionStatus status, Instant now);
}
