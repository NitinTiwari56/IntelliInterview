package com.InterviewPrep.InterviewPrep.repository;

import com.InterviewPrep.InterviewPrep.model.InterviewSessionEntity;
import com.InterviewPrep.InterviewPrep.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, String> {

    List<InterviewSessionEntity> findByStudentIdOrderByStartedAtDesc(Long studentId);

    List<InterviewSessionEntity> findByStudentIdAndStatusOrderByStartedAtDesc(Long studentId, SessionStatus status);

    long countByStudentId(Long studentId);
}
