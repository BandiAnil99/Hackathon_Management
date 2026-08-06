package com.hackathon.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hackathon.dto.PanelistDashboardEventSummary;
import com.hackathon.dto.PanelistDashboardResponse;
import com.hackathon.service.DashboardService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();
    }

    @Test
    void panelistMeEndpointReturnsDashboard() throws Exception {
        when(dashboardService.panelistDashboardByEmail("deekshi@example.com")).thenReturn(
                new PanelistDashboardResponse(
                        5L,
                        "Deekshi",
                        "deekshi@example.com",
                        1,
                        3,
                        3,
                        List.of(new PanelistDashboardEventSummary(
                                101L,
                                "Buildathon",
                                3,
                                3,
                                List.of(com.hackathon.entity.FeedbackType.DESIGN),
                                LocalDateTime.parse("2026-07-20T11:00:00")))));

        Principal principal = () -> "deekshi@example.com";

        mockMvc.perform(get("/api/dashboard/panelist/me")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.panelistEmail").value("deekshi@example.com"))
                .andExpect(jsonPath("$.eventsHandled").value(1));
    }
}