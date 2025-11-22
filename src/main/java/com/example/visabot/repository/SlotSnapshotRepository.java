package com.example.visabot.repository;

import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.entity.VisaCenter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotSnapshotRepository extends JpaRepository<SlotSnapshot, Long> {
    Optional<SlotSnapshot> findTopByVisaCenterOrderByCreatedAtDesc(VisaCenter visaCenter);
}
