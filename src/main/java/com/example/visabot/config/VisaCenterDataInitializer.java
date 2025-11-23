package com.example.visabot.config;

import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.VisaCenterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisaCenterDataInitializer implements CommandLineRunner {

    private final VisaCenterRepository visaCenterRepository;

    @Override
    public void run(String... args) {
        if (visaCenterRepository.count() > 0) {
            return;
        }

        List<VisaCenter> defaultCenters = List.of(
                createCenter("Finland", "Helsinki", "Test center", "TEST", true),
                createCenter("Finland", "Saint Petersburg", "VFS Global Finland", "VFS", true),
                createCenter("France", "Moscow", "VFS Global France", "VFS", true),
                createCenter("Spain", "Moscow", "BLS Spain Moscow", "BLS", true)
        );

        visaCenterRepository.saveAll(defaultCenters);
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
