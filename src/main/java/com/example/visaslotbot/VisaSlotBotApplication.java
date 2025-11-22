package com.example.visaslotbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisaSlotBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisaSlotBotApplication.class, args);
    }
}
