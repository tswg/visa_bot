package com.example.visabot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "slot_events")
@Getter
@Setter
public class SlotEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_center_id")
    private VisaCenter visaCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_snapshot_id")
    private SlotSnapshot snapshot;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
