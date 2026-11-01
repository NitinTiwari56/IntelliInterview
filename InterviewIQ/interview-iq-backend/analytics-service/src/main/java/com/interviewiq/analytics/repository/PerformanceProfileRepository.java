package com.interviewiq.analytics.repository;

import com.interviewiq.analytics.model.PerformanceProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerformanceProfileRepository extends JpaRepository<PerformanceProfileEntity, Long> {

    Optional<PerformanceProfileEntity> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}
