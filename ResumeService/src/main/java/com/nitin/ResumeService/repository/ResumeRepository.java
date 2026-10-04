package com.nitin.ResumeService.repository;

import com.nitin.ResumeService.model.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {

    List<ResumeEntity> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    Optional<ResumeEntity> findTopByStudentIdOrderByUploadedAtDesc(Long studentId);

    Optional<ResumeEntity> findByResumeIdAndStudentId(Long resumeId, Long studentId);

    long countByStudentId(Long studentId);

    @Query("SELECT AVG(r.atsScore) FROM ResumeEntity r WHERE r.studentId = :studentId")
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);
}
