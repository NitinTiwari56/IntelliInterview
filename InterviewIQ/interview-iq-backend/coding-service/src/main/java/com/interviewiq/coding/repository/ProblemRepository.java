package com.interviewiq.coding.repository;

import com.interviewiq.coding.entity.Problem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByPublishedTrueOrderByIdAsc();

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "testCases")
    Optional<Problem> findWithTestCasesBySlugAndPublishedTrue(String slug);

    @EntityGraph(attributePaths = "testCases")
    Optional<Problem> findWithTestCasesById(Long id);

    @EntityGraph(attributePaths = "testCases")
    Optional<Problem> findWithTestCasesBySlug(String slug);

    @EntityGraph(attributePaths = "testCases")
    List<Problem> findAllByOrderByIdAsc();
}
