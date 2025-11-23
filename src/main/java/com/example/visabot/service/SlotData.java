package com.example.visabot.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class SlotData {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final List<LocalDate> availableDates;

    public SlotData(List<LocalDate> availableDates) {
        this.availableDates = availableDates == null ? Collections.emptyList() : new ArrayList<>(availableDates);
    }

    public String hash() {
        List<String> sorted = availableDates.stream()
                .filter(Objects::nonNull)
                .map(FORMATTER::format)
                .sorted()
                .collect(Collectors.toList());
        String joined = String.join("|", sorted);
        return sha256(joined);
    }

    public String toSummaryString() {
        if (availableDates.isEmpty()) {
            return "No available dates.";
        }
        return "Available dates: " + availableDates.stream()
                .sorted()
                .map(FORMATTER::format)
                .collect(Collectors.joining(", "));
    }

    public String toHumanReadableString() {
        if (availableDates.isEmpty()) {
            return "No available dates found at this time.";
        }
        return "Found available slots on: " + availableDates.stream()
                .sorted()
                .map(FORMATTER::format)
                .collect(Collectors.joining(", "));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
