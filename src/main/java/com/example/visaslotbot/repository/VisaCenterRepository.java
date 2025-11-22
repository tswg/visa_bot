package com.example.visaslotbot.repository;

import com.example.visaslotbot.model.VisaCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisaCenterRepository extends JpaRepository<VisaCenter, Long> {
    List<VisaCenter> findByActiveTrue();

    Optional<VisaCenter> findByProviderAndActiveTrue(String provider);
}
