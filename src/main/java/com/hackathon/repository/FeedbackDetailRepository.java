package com.hackathon.repository;

import com.hackathon.entity.FeedbackDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackDetailRepository extends JpaRepository<FeedbackDetail, Long> {

    List<FeedbackDetail> findByFeedbackId(Long feedbackId);

    List<FeedbackDetail> findByFeedbackIdIn(List<Long> feedbackIds);

    void deleteByFeedbackId(Long feedbackId);
}
