package com.interviewiq.aptitude.repository;

import com.interviewiq.aptitude.entity.AptitudeTest;
import com.interviewiq.aptitude.entity.TestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AptitudeTestRepository extends JpaRepository<AptitudeTest, Long> {

    // @EntityGraph = questions ko saath hi load karo (lazy loading ka error nahi aayega)
    @EntityGraph(attributePaths = "questions")
    List<AptitudeTest> findByUserIdAndStatus(Long userId, TestStatus status);

    // userId bhi check hota hai, taaki koi doosre ka test na dekh sake
    @EntityGraph(attributePaths = "questions")
    Optional<AptitudeTest> findByIdAndUserId(Long id, Long userId);

    /** Scheduler ke liye: jin tests ka time khatam ho gaya par submit nahi hue. */
    @EntityGraph(attributePaths = "questions")
    List<AptitudeTest> findByStatusAndExpiresAtBefore(TestStatus status, Instant before);

    List<AptitudeTest> findByUserIdAndStatusOrderBySubmittedAtDesc(Long userId, TestStatus status);
}
