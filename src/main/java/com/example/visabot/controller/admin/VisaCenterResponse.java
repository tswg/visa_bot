package com.example.visabot.controller.admin;

import java.time.LocalDateTime;

public record VisaCenterResponse(
        Long id,
        String country,
        String city,
        String name,
        String provider,
        String checkUrl,
        boolean active,
        LocalDateTime createdAt,
        String lastSnapshotHash) {}
