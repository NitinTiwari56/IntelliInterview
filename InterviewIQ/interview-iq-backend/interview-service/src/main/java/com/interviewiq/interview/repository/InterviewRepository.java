package com.interviewiq.interview.repository;

import com.interviewiq.interview.model.InterviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<InterviewEntity, Long> {

    List<InterviewEntity> findBySessionIdOrderByQuestionNumberAsc(String sessionId);

    List<InterviewEntity> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<InterviewEntity> findByStudentIdAndTypeOrderByCreatedAtDesc(Long studentId, String type);
}
