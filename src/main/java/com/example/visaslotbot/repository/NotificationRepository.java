package com.example.visaslotbot.repository;

import com.example.visaslotbot.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
