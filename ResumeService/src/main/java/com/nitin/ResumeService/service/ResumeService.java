package com.nitin.ResumeService.service;

import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import com.nitin.ResumeService.dto.ResumeHistoryResponse;
import com.nitin.ResumeService.model.RoleSkillProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResumeService {

    /**
     * Uploads and parses candidate PDF resume, analyzes ATS compatibility,
     * extracts skills, evaluates against target role, stores analysis, and publishes event.
     */
    ResumeAnalysisResponse uploadAndAnalyzeResume(MultipartFile file, Long studentId, String targetRole);

    /**
     * Fetches history of previous resume analyses for a student (REQ-RES-10).
     */
    ResumeHistoryResponse getStudentResumeHistory(Long studentId);

    /**
     * Retrieves specific resume analysis by ID for a student.
     */
    ResumeAnalysisResponse getResumeAnalysisById(Long resumeId, Long studentId);

    /**
     * Retrieves the latest analyzed resume for a student.
     */
    ResumeAnalysisResponse getLatestResumeAnalysis(Long studentId);

    /**
     * Returns list of supported target roles and skill profiles.
     */
    List<RoleSkillProfile> getAvailableTargetRoles();

    /**
     * Returns service health and storage metadata.
     */
    Map<String, Object> getServiceHealth();
}
