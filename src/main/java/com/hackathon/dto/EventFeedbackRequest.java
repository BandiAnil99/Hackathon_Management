package com.hackathon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EventFeedbackRequest(
        @NotNull Long eventId,
        @NotNull @Min(1) @Max(5) Integer laptopPerformance,
        @NotNull @Min(1) @Max(5) Integer wifiConnection,
        @NotNull @Min(1) @Max(5) Integer environment,
        @NotNull @Min(1) @Max(5) Integer sessionQuality,
        String comments
) {
}
