package com.example.visabot.service;

import com.example.visabot.entity.VisaCenter;

public interface SlotScraper {

    SlotData fetchSlots(VisaCenter center);
}
