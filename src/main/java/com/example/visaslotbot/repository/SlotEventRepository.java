package com.example.visaslotbot.repository;

import com.example.visaslotbot.model.SlotEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotEventRepository extends JpaRepository<SlotEvent, Long> {
}
