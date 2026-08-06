package com.hackathon.controller;

import com.hackathon.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LocationController {

    private final DashboardService dashboardService;

    public LocationController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/locations")
    public List<String> findAllLocations() {
        return dashboardService.locations();
    }
}
