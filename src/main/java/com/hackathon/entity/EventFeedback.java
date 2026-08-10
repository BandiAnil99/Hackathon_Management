package com.hackathon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Anonymous attendee feedback submitted through an event's public QR code. */
@Entity
@Table(name = "event_feedback")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private Integer laptopPerformance;
    private Integer wifiConnection;
    private Integer environment;
    private Integer sessionQuality;
    private String comments;
    private LocalDateTime submittedAt;
}
