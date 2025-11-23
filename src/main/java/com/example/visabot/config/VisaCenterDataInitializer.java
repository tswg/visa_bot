package com.example.visabot.config;

import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.VisaCenterRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class VisaCenterDataInitializer implements CommandLineRunner {

    private final VisaCenterRepository visaCenterRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (visaCenterRepository.count() > 0) {
            log.debug("Visa centers already present, skipping initialization");
            return;
        }

        List<VisaCenter> centers = defaultCenters();

        visaCenterRepository.saveAll(centers);
        log.info("Initialized {} visa centers: {}", centers.size(), centers.stream()
                .map(VisaCenter::getName)
                .collect(Collectors.joining(", ")));
    }

    private List<VisaCenter> defaultCenters() {
        return List.of(
                createCenter("Finland", "Helsinki", "Test center", "TEST", true),
                createCenter("Finland", "Saint Petersburg", "VFS Global Finland", "VFS", true),
                createCenter("France", "Moscow", "VFS Global France", "VFS", true),
                createCenter("Spain", "Moscow", "BLS Spain Moscow", "BLS", true));
    }

    private VisaCenter createCenter(String country, String city, String name, String provider, boolean active) {
        VisaCenter center = new VisaCenter();
        center.setCountry(country);
        center.setCity(city);
        center.setName(name);
        center.setProvider(provider);
        center.setActive(active);
        return center;
    }
}
