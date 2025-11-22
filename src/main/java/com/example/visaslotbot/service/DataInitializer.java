package com.example.visaslotbot.service;

import com.example.visaslotbot.model.VisaCenter;
import com.example.visaslotbot.repository.VisaCenterRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final VisaCenterRepository visaCenterRepository;

    @PostConstruct
    public void seedData() {
        if (visaCenterRepository.count() == 0) {
            VisaCenter center = VisaCenter.builder()
                    .country("Finland")
                    .city("Helsinki")
                    .name("Test center")
                    .provider(VisaCenterService.TEST_PROVIDER)
                    .checkUrl("https://example.com/test-center")
                    .active(true)
                    .build();
            visaCenterRepository.save(center);
            log.info("Created test visa center entry");
        }
    }
}
