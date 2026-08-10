package com.hackathon.service;

import com.hackathon.dto.EventFeedbackAreaSummary;
import com.hackathon.dto.EventFeedbackInsightsResponse;
import com.hackathon.dto.EventFeedbackRequest;
import com.hackathon.dto.EventFeedbackSubmitResponse;
import com.hackathon.entity.EventFeedback;
import com.hackathon.exception.ResourceNotFoundException;
import com.hackathon.repository.EventFeedbackRepository;
import com.hackathon.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EventFeedbackService {

    private final EventRepository eventRepository;
    private final EventFeedbackRepository eventFeedbackRepository;

    public EventFeedbackService(EventRepository eventRepository, EventFeedbackRepository eventFeedbackRepository) {
        this.eventRepository = eventRepository;
        this.eventFeedbackRepository = eventFeedbackRepository;
    }

    public EventFeedbackSubmitResponse submit(EventFeedbackRequest request) {
        if (!eventRepository.existsById(request.eventId())) {
            throw new ResourceNotFoundException("Event not found: " + request.eventId());
        }
        eventFeedbackRepository.save(EventFeedback.builder()
                .eventId(request.eventId())
                .laptopPerformance(request.laptopPerformance())
                .wifiConnection(request.wifiConnection())
                .environment(request.environment())
                .sessionQuality(request.sessionQuality())
                .comments(normalizeComment(request.comments()))
                .submittedAt(LocalDateTime.now())
                .build());
        return new EventFeedbackSubmitResponse("SUCCESS", "Thank you for your feedback.");
    }

    public EventFeedbackInsightsResponse insights(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        List<EventFeedback> feedback = eventFeedbackRepository.findByEventIdOrderBySubmittedAtDesc(eventId);
        Map<String, Double> averages = new LinkedHashMap<>();
        averages.put("Laptop performance", average(feedback, EventFeedback::getLaptopPerformance));
        averages.put("Wi-Fi connection", average(feedback, EventFeedback::getWifiConnection));
        averages.put("Environment", average(feedback, EventFeedback::getEnvironment));
        averages.put("Session quality", average(feedback, EventFeedback::getSessionQuality));

        List<EventFeedbackAreaSummary> areas = averages.entrySet().stream()
                .map(entry -> new EventFeedbackAreaSummary(entry.getKey(), entry.getValue()))
                .toList();
        List<String> comments = feedback.stream().map(EventFeedback::getComments)
                .filter(comment -> comment != null && !comment.isBlank()).limit(10).toList();
        return new EventFeedbackInsightsResponse(eventId, feedback.size(),
                areas.stream().sorted(Comparator.comparing(EventFeedbackAreaSummary::averageRating).reversed()).limit(2).toList(),
                areas.stream().sorted(Comparator.comparing(EventFeedbackAreaSummary::averageRating)).limit(2).toList(),
                comments);
    }

    private double average(List<EventFeedback> feedback, java.util.function.Function<EventFeedback, Integer> selector) {
        return feedback.isEmpty() ? 0.0 : Math.round(feedback.stream().map(selector)
                .mapToInt(Integer::intValue).average().orElse(0.0) * 100.0) / 100.0;
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) return null;
        return comment.trim().length() > 2000 ? comment.trim().substring(0, 2000) : comment.trim();
    }
}
