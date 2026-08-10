package com.hackathon.dto;

import java.util.List;

public record EventFeedbackInsightsResponse(
        Long eventId,
        long responses,
        List<EventFeedbackAreaSummary> strengths,
        List<EventFeedbackAreaSummary> improvementAreas,
        List<String> recentComments
) {
}
