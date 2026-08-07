package com.hackathon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hackathon.util.FrontendUrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RegistrationRedirectControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RegistrationRedirectController(
                "https://resilient-shortbread-479bd6.netlify.app/"))
                .build();
    }

    @Test
    void registrationRedirectPreservesEventIdAndUsesConfiguredFrontend() throws Exception {
        mockMvc.perform(get("/participants/register").param("eventId", "42"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "https://resilient-shortbread-479bd6.netlify.app/participants/register?eventId=42"));
    }

    @Test
    void checkInRedirectPreservesEventIdAndUsesConfiguredFrontend() throws Exception {
        mockMvc.perform(get("/check-in").param("eventId", "42"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "https://resilient-shortbread-479bd6.netlify.app/check-in?eventId=42"));
    }

    @Test
    void frontendUrlBuilderRemovesTrailingSlash() {
        org.junit.jupiter.api.Assertions.assertEquals(
                "https://resilient-shortbread-479bd6.netlify.app/panelist/register?token=abc",
                FrontendUrlBuilder.build("https://resilient-shortbread-479bd6.netlify.app/",
                        "/panelist/register?token=abc"));
    }
}
