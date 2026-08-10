package com.hackathon.controller;

import com.hackathon.dto.EventFeedbackInsightsResponse;
import com.hackathon.dto.EventFeedbackRequest;
import com.hackathon.dto.EventFeedbackSubmitResponse;
import com.hackathon.service.EventFeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-feedback")
public class EventFeedbackController {

    private final EventFeedbackService eventFeedbackService;

    public EventFeedbackController(EventFeedbackService eventFeedbackService) {
        this.eventFeedbackService = eventFeedbackService;
    }

    @PostMapping
    public EventFeedbackSubmitResponse submit(@Valid @RequestBody EventFeedbackRequest request) {
        return eventFeedbackService.submit(request);
    }

    @GetMapping("/event/{eventId}/insights")
    @PreAuthorize("hasRole('ADMIN')")
    public EventFeedbackInsightsResponse insights(@PathVariable Long eventId) {
        return eventFeedbackService.insights(eventId);
    }
}
