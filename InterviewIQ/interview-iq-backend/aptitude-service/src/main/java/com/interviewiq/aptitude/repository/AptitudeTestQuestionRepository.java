package com.interviewiq.aptitude.repository;

import com.interviewiq.aptitude.entity.AptitudeTestQuestion;
import com.interviewiq.aptitude.entity.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AptitudeTestQuestionRepository extends JpaRepository<AptitudeTestQuestion, Long> {

    /** Rows: [category, totalQuestions, correctCount] - sirf SUBMITTED tests ke. */
    @Query("select q.category, count(q), sum(case when q.correct = true then 1 else 0 end) "
            + "from AptitudeTestQuestion q "
            + "where q.test.userId = :userId and q.test.status = :status "
            + "group by q.category")
    List<Object[]> categoryStats(@Param("userId") Long userId, @Param("status") TestStatus status);
}
