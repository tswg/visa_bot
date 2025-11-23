package com.example.visabot.controller.admin;

import java.util.List;

public record SlotInspectionResponse(
        SlotSnapshotResponse lastSnapshot,
        List<SlotEventResponse> recentEvents) {}
