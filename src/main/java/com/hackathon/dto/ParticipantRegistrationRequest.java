package com.hackathon.dto;

import com.hackathon.entity.CandidateType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParticipantRegistrationRequest(
        @NotNull Long eventId,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotNull CandidateType candidateType,
        String candidateId,
        @NotBlank String techStack,
        String phone,
        @Min(0) Integer experienceYears
) {
}
