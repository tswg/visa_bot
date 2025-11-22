package com.example.visabot.service;

import com.example.visabot.entity.VisaCenter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class SlotScraper {

    public SlotData fetchSlots(VisaCenter center) {
        String rawData = "Slots for " + center.getName() + " at " + Instant.now();
        String hash = sha256(rawData);
        return new SlotData(rawData, hash);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
