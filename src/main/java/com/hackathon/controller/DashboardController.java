package com.hackathon.controller;

import com.hackathon.dto.DashboardSummary;
import com.hackathon.dto.EventFeedbackInsightsResponse;
import com.hackathon.service.EventFeedbackService;
import com.hackathon.dto.PanelistDashboardResponse;
import com.hackathon.service.DashboardService;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final EventFeedbackService eventFeedbackService;

    public DashboardController(DashboardService dashboardService, EventFeedbackService eventFeedbackService) {
        this.dashboardService = dashboardService;
        this.eventFeedbackService = eventFeedbackService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardSummary summary() {
        return dashboardService.summary();
    }

    @GetMapping("/events/{eventId}/feedback-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public EventFeedbackInsightsResponse feedbackInsights(@PathVariable Long eventId) {
        return eventFeedbackService.insights(eventId);
    }

    @GetMapping("/panelist/me")
    @PreAuthorize("hasRole('PANELIST')")
    public PanelistDashboardResponse myPanelistDashboard(Principal principal) {
        return dashboardService.panelistDashboardByEmail(principal.getName());
    }

    @GetMapping("/panelist/{panelistId}")
    @PreAuthorize("hasAnyRole('ADMIN','PANELIST')")
    public PanelistDashboardResponse panelistDashboard(@PathVariable Long panelistId) {
        return dashboardService.panelistDashboard(panelistId);
    }
}
