package com.hackathon.controller;

import com.hackathon.dto.HackathonsByLocationResponse;
import com.hackathon.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardAnalyticsController {

    private final DashboardService dashboardService;

    public DashboardAnalyticsController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/hackathons-by-location")
    public List<HackathonsByLocationResponse> hackathonsByLocation() {
        return dashboardService.hackathonsByLocation();
    }
}
