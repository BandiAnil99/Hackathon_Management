package com.hackathon.dto;

public record HackathonsByLocationResponse(
        String location,
        long hackathonCount
) {
}
