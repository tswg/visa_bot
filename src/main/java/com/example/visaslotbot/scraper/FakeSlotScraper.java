package com.example.visaslotbot.scraper;

import com.example.visaslotbot.model.VisaCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class FakeSlotScraper implements SlotScraper {

    private final Random random = new Random();

    @Override
    public SlotData fetchSlots(VisaCenter center) {
        int chance = random.nextInt(10);
        List<LocalDate> dates = new ArrayList<>();
        if (chance > 2) {
            int count = random.nextInt(3) + 1;
            for (int i = 0; i < count; i++) {
                dates.add(LocalDate.now().plusDays(random.nextInt(30) + 1));
            }
        }
        log.info("Fake scraper fetched {} dates for center {}", dates.size(), center.getName());
        return SlotData.builder()
                .availableDates(dates)
                .build();
    }
}
