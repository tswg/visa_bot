package com.example.visabot.controller.admin;

import java.time.LocalDateTime;

public record SlotSnapshotResponse(LocalDateTime createdAt, String rawData, String hash) {}
