package com.interviewiq.interview.repository;

import com.interviewiq.interview.model.InterviewSessionEntity;
import com.interviewiq.interview.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, String> {

    List<InterviewSessionEntity> findByStudentIdOrderByStartedAtDesc(Long studentId);

    List<InterviewSessionEntity> findByStudentIdAndStatusOrderByStartedAtDesc(Long studentId, SessionStatus status);

    long countByStudentId(Long studentId);
}
