package com.RoadMapService.RoadMapService.repository;

import com.RoadMapService.RoadMapService.model.PerformanceProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerformanceProfileRepository extends JpaRepository<PerformanceProfileEntity, Long> {

    Optional<PerformanceProfileEntity> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}
