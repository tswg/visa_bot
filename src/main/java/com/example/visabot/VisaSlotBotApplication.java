package com.example.visabot;

import com.example.visabot.config.BotProperties;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.VisaCenterRepository;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BotProperties.class)
public class VisaSlotBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisaSlotBotApplication.class, args);
    }

    @Bean
    CommandLineRunner initVisaCenter(VisaCenterRepository visaCenterRepository) {
        return args -> {
            Optional<VisaCenter> existing = visaCenterRepository
                    .findByCountryAndCityAndName("Finland", "Helsinki", "Test center");
            if (existing.isEmpty()) {
                VisaCenter center = new VisaCenter();
                center.setCountry("Finland");
                center.setCity("Helsinki");
                center.setName("Test center");
                center.setProvider("TEST");
                center.setActive(true);
                visaCenterRepository.save(center);
            }
        };
    }
}
