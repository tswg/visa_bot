package com.example.visabot.repository;

import com.example.visabot.entity.SlotEvent;
import com.example.visabot.entity.VisaCenter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotEventRepository extends JpaRepository<SlotEvent, Long> {
    List<SlotEvent> findTop5ByVisaCenterOrderByCreatedAtDesc(VisaCenter visaCenter);
}
