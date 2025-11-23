package com.example.visabot.scraper;

import com.example.visabot.entity.VisaCenter;
import com.example.visabot.service.SlotData;
import com.example.visabot.service.SlotScraper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpSlotScraper implements SlotScraper {

    private final RestTemplate restTemplate;

    @Override
    public SlotData fetchSlots(VisaCenter center) {
        if (center.getCheckUrl() == null || center.getCheckUrl().isBlank()) {
            log.warn("Check URL is missing for center {}", center.getId());
            return new SlotData(Collections.emptyList());
        }

        try {
            SlotApiResponse response = restTemplate.getForObject(center.getCheckUrl(), SlotApiResponse.class);
            List<LocalDate> dates = response == null || response.getAvailableDates() == null
                    ? Collections.emptyList()
                    : response.getAvailableDates().stream()
                            .map(LocalDate::parse)
                            .collect(Collectors.toList());
            return new SlotData(dates);
        } catch (Exception e) {
            log.error("Failed to fetch slots from {} for center {}", center.getCheckUrl(), center.getId(), e);
            return new SlotData(Collections.emptyList());
        }
    }

    @Data
    public static class SlotApiResponse {
        private List<String> availableDates;
    }
}
