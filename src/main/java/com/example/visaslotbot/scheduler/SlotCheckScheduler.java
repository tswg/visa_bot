package com.example.visaslotbot.scheduler;

import com.example.visaslotbot.service.SlotCheckService;
import com.example.visaslotbot.service.VisaCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotCheckScheduler {

    private final SlotCheckService slotCheckService;
    private final VisaCenterService visaCenterService;

    @Scheduled(fixedDelayString = "${app.slots.check-interval-ms:120000}")
    public void runCheck() {
        visaCenterService.activeCenters()
                .forEach(center -> {
                    log.info("Checking slots for center {}", center.getName());
                    slotCheckService.checkCenter(center);
                });
    }
}
