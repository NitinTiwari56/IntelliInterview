package com.RoadMapService.RoadMapService.repository;

import com.RoadMapService.RoadMapService.model.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<RoadmapEntity, Long> {

    Optional<RoadmapEntity> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}
