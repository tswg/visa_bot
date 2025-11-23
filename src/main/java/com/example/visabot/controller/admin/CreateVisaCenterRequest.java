package com.example.visabot.controller.admin;

import jakarta.validation.constraints.NotBlank;

public record CreateVisaCenterRequest(
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String name,
        @NotBlank String provider,
        @NotBlank String checkUrl) {}
