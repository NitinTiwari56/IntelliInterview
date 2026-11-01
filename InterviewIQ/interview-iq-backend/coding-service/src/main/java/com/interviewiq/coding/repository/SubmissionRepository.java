package com.interviewiq.coding.repository;

import com.interviewiq.coding.entity.Submission;
import com.interviewiq.coding.entity.Verdict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @EntityGraph(attributePaths = "problem")
    List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "problem")
    List<Submission> findByUserIdAndProblemSlugOrderByCreatedAtDesc(Long userId, String slug);

    @EntityGraph(attributePaths = "problem")
    Optional<Submission> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndVerdict(Long userId, Verdict verdict);

    /** Rows: [topic, totalSubmissions, acceptedSubmissions]. */
    @Query("select s.problem.topic, count(s), sum(case when s.verdict = :accepted then 1 else 0 end) "
            + "from Submission s where s.userId = :userId group by s.problem.topic")
    List<Object[]> topicSummary(@Param("userId") Long userId, @Param("accepted") Verdict accepted);

    /** Rows: [problemId, acceptedCount]  -> user ne jin problems ko try kiya, aur kitni baar accept hua. */
    @Query("select s.problem.id, sum(case when s.verdict = :accepted then 1 else 0 end) "
            + "from Submission s where s.userId = :userId group by s.problem.id")
    List<Object[]> problemSummary(@Param("userId") Long userId, @Param("accepted") Verdict accepted);
}
