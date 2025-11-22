package com.example.visabot.repository;

import com.example.visabot.entity.VisaCenter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisaCenterRepository extends JpaRepository<VisaCenter, Long> {
    List<VisaCenter> findByActiveTrue();

    Optional<VisaCenter> findByCountryAndCityAndName(String country, String city, String name);
}
