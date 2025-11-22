package com.example.visaslotbot.scraper;

import com.example.visaslotbot.model.VisaCenter;

public interface SlotScraper {
    SlotData fetchSlots(VisaCenter center);
}
