package com.example.visabot.controller.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateVisaCenterRequest(
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String name,
        @NotBlank String provider,
        @NotBlank String checkUrl,
        @NotNull Boolean active) {}
