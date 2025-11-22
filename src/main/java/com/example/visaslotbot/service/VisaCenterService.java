package com.example.visaslotbot.service;

import com.example.visaslotbot.model.VisaCenter;
import com.example.visaslotbot.repository.VisaCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisaCenterService {

    public static final String TEST_PROVIDER = "TEST";

    private final VisaCenterRepository visaCenterRepository;

    public List<VisaCenter> activeCenters() {
        return visaCenterRepository.findByActiveTrue();
    }

    public Optional<VisaCenter> findActiveTestCenter() {
        return visaCenterRepository.findByProviderAndActiveTrue(TEST_PROVIDER);
    }
}
