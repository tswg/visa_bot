package com.example.visabot.repository;

import com.example.visabot.entity.Subscription;
import com.example.visabot.entity.SubscriptionPlan;
import com.example.visabot.entity.SubscriptionStatus;
import com.example.visabot.entity.User;
import com.example.visabot.entity.VisaCenter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByVisaCenterAndStatusAndValidToAfter(VisaCenter visaCenter, SubscriptionStatus status,
            LocalDateTime now);

    Optional<Subscription> findByUserAndVisaCenter(User user, VisaCenter visaCenter);

    Optional<Subscription> findByIdAndUser(Long id, User user);

    List<Subscription> findByUserAndStatusAndValidToAfter(User user, SubscriptionStatus status, LocalDateTime now);

    @Query("select s from Subscription s "
            + "join fetch s.visaCenter vc "
            + "where s.user = :user and s.status = :status and s.validTo > :now")
    List<Subscription> findActiveWithVisaCenter(@Param("user") User user,
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now);

    @Query("select s from Subscription s "
            + "join fetch s.user u "
            + "where s.visaCenter = :visaCenter and s.status = :status and s.validTo > :now")
    List<Subscription> findActiveByVisaCenterWithUser(@Param("visaCenter") VisaCenter visaCenter,
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now);

    @Query("select s from Subscription s "
            + "join fetch s.user u "
            + "where s.visaCenter = :visaCenter and s.status = :status and s.validTo > :now "
            + "and (s.plan = :plan or (:plan = com.example.visabot.entity.SubscriptionPlan.BASIC and s.plan is null))")
    List<Subscription> findActiveByVisaCenterWithUserAndPlan(@Param("visaCenter") VisaCenter visaCenter,
            @Param("status") SubscriptionStatus status,
            @Param("plan") SubscriptionPlan plan,
            @Param("now") LocalDateTime now);
}
