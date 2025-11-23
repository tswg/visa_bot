package com.example.visabot.controller.admin;

import com.example.visabot.entity.SlotSnapshot;
import com.example.visabot.entity.VisaCenter;
import com.example.visabot.repository.SlotEventRepository;
import com.example.visabot.repository.SlotSnapshotRepository;
import com.example.visabot.repository.VisaCenterRepository;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VisaCenterRepository visaCenterRepository;
    private final SlotSnapshotRepository slotSnapshotRepository;
    private final SlotEventRepository slotEventRepository;

    @GetMapping("/centers")
    public List<VisaCenterResponse> getCenters() {
        return visaCenterRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/centers")
    public VisaCenterResponse createCenter(@Valid @RequestBody CreateVisaCenterRequest request) {
        VisaCenter center = new VisaCenter();
        center.setCountry(request.country());
        center.setCity(request.city());
        center.setName(request.name());
        center.setProvider(request.provider());
        center.setCheckUrl(request.checkUrl());
        center.setActive(true);
        center.setCreatedAt(LocalDateTime.now());

        VisaCenter savedCenter = visaCenterRepository.save(center);
        return toResponse(savedCenter);
    }

    @PutMapping("/centers/{id}")
    public VisaCenterResponse updateCenter(
            @PathVariable Long id, @Valid @RequestBody UpdateVisaCenterRequest request) {
        VisaCenter center = visaCenterRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visa center not found"));

        center.setCountry(request.country());
        center.setCity(request.city());
        center.setName(request.name());
        center.setProvider(request.provider());
        center.setCheckUrl(request.checkUrl());
        center.setActive(request.active());

        VisaCenter savedCenter = visaCenterRepository.save(center);
        return toResponse(savedCenter);
    }

    @DeleteMapping("/centers/{id}")
    public Map<String, String> deactivateCenter(@PathVariable Long id) {
        VisaCenter center = visaCenterRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visa center not found"));

        center.setActive(false);
        visaCenterRepository.save(center);

        return Map.of("status", "deactivated");
    }

    @GetMapping("/centers/{id}/slots")
    public SlotInspectionResponse getSlotInspection(@PathVariable Long id) {
        VisaCenter center = visaCenterRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visa center not found"));

        SlotSnapshotResponse snapshotResponse = slotSnapshotRepository
                .findTopByVisaCenterOrderByCreatedAtDesc(center)
                .map(snapshot -> new SlotSnapshotResponse(
                        snapshot.getCreatedAt(), snapshot.getRawData(), snapshot.getRawDataHash()))
                .orElse(null);

        List<SlotEventResponse> eventResponses = slotEventRepository
                .findTop5ByVisaCenterOrderByCreatedAtDesc(center)
                .stream()
                .map(event -> new SlotEventResponse(event.getCreatedAt(), event.getDescription()))
                .toList();

        return new SlotInspectionResponse(snapshotResponse, eventResponses);
    }

    private VisaCenterResponse toResponse(VisaCenter center) {
        String lastSnapshotHash = slotSnapshotRepository
                .findTopByVisaCenterOrderByCreatedAtDesc(center)
                .map(SlotSnapshot::getRawDataHash)
                .orElse("none");

        return new VisaCenterResponse(
                center.getId(),
                center.getCountry(),
                center.getCity(),
                center.getName(),
                center.getProvider(),
                center.getCheckUrl(),
                center.isActive(),
                center.getCreatedAt(),
                lastSnapshotHash);
    }
}
