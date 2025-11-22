package com.example.visabot.repository;

import com.example.visabot.entity.SlotEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotEventRepository extends JpaRepository<SlotEvent, Long> {
}
