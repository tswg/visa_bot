package com.example.visabot.scraper;

import com.example.visabot.entity.VisaCenter;
import com.example.visabot.service.SlotData;
import com.example.visabot.service.SlotScraper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FakeSlotScraper implements SlotScraper {

    @Override
    public SlotData fetchSlots(VisaCenter center) {
        log.info("Fetching fake slots for center {}", center.getId());
        int slotCount = ThreadLocalRandom.current().nextInt(0, 4);
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < slotCount; i++) {
            LocalDate date = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 60));
            dates.add(date);
        }
        return new SlotData(new ArrayList<>(dates));
    }
}
