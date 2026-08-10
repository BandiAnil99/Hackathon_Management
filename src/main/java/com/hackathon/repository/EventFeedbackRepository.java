package com.hackathon.repository;

import com.hackathon.entity.EventFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {
    List<EventFeedback> findByEventIdOrderBySubmittedAtDesc(Long eventId);
    void deleteByEventId(Long eventId);
}
