package com.example.visaslotbot.scraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotData {
    private List<LocalDate> availableDates;

    public String hash() {
        List<LocalDate> dates = availableDates == null ? Collections.emptyList() : availableDates;
        String joined = dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot calculate hash", e);
        }
    }

    public String toSummaryString() {
        List<LocalDate> dates = availableDates == null ? Collections.emptyList() : availableDates;
        if (dates.isEmpty()) {
            return "No slots";
        }
        return "Slots on " + dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(", "));
    }

    public String toHumanReadableString() {
        List<LocalDate> dates = availableDates == null ? Collections.emptyList() : availableDates;
        if (dates.isEmpty()) {
            return "Нет доступных дат";
        }
        return dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(", "));
    }
}
