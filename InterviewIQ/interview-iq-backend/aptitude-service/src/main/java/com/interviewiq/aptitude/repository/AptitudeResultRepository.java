package com.interviewiq.aptitude.repository;

import com.interviewiq.aptitude.entity.AptitudeResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AptitudeResultRepository extends JpaRepository<AptitudeResult, Long> {

    List<AptitudeResult> findByUserIdOrderBySubmittedAtDesc(Long userId);

    // userId bhi check hota hai, taaki koi doosre ka result na dekh sake
    @EntityGraph(attributePaths = "categories")
    Optional<AptitudeResult> findByIdAndUserId(Long id, Long userId);

    /** Ek row: [totalScore, totalMarks, attempts]. Koi result nahi ho toh score/marks null. */
    @Query("select sum(r.score), sum(r.totalMarks), count(r) from AptitudeResult r where r.userId = :userId")
    List<Object[]> totals(@Param("userId") Long userId);

    /** Rows: [category, totalQuestions, correctCount] - saare attempts ka sum. */
    @Query("select c.category, sum(c.totalQuestions), sum(c.correctCount) "
            + "from AptitudeResultCategory c where c.result.userId = :userId group by c.category")
    List<Object[]> categoryStats(@Param("userId") Long userId);
}
