package com.hackathon.entity;

import com.hackathon.entity.CandidateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String participantCode;
    private Long eventId;
    private String name;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "candidate_type")
    private CandidateType candidateType;
    @Column(name = "candidate_id")
    private String candidateId;
    private String techStack;
    private String phone;
    private Integer experienceYears;
    private String resumeUrl;
    private String photoUrl;
    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String resumeAnalysisJson;

    @Enumerated(EnumType.STRING)
    private ParticipantStatus status;

    @Builder.Default
    private Boolean aiAnalysisPending = false;

    @Builder.Default
    private Integer aiAnalysisAttemptCount = 0;

    private LocalDateTime lastAiAnalysisAttempt;

    @Column(columnDefinition = "TEXT")
    private String lastAiAnalysisError;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AiAnalysisStatus aiAnalysisStatus = AiAnalysisStatus.NOT_STARTED;

    @Builder.Default
    private Integer feedbackCount = 0;

    @Builder.Default
    private Double cumulativeAvgScore = 0.0;
}
