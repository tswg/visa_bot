package com.example.visaslotbot.repository;

import com.example.visaslotbot.model.SlotSnapshot;
import com.example.visaslotbot.model.VisaCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlotSnapshotRepository extends JpaRepository<SlotSnapshot, Long> {
    Optional<SlotSnapshot> findTopByVisaCenterOrderBySnapshotTimeDesc(VisaCenter visaCenter);
}
