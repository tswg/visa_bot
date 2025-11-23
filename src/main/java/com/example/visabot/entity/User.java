package com.example.visabot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "bot_users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long telegramId;

    private String username;

    private Boolean notificationsEnabled;

    private Boolean dndNightEnabled;

    private LocalDate notifyFromDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
