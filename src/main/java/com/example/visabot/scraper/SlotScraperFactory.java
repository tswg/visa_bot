package com.example.visabot.scraper;

import com.example.visabot.entity.VisaCenter;
import com.example.visabot.service.SlotScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlotScraperFactory {

    private final FakeSlotScraper fakeSlotScraper;
    private final HttpSlotScraper httpSlotScraper;

    public SlotScraper getScraperFor(VisaCenter center) {
        String provider = center.getProvider();
        if (provider == null) {
            return fakeSlotScraper;
        }
        return switch (provider.toUpperCase()) {
            case "HTTP" -> httpSlotScraper;
            case "TEST" -> fakeSlotScraper;
            default -> fakeSlotScraper;
        };
    }
}
