package com.nitin.ResumeService.controller;

import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import com.nitin.ResumeService.dto.ResumeHistoryResponse;
import com.nitin.ResumeService.model.RoleSkillProfile;
import com.nitin.ResumeService.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Upload and analyze a PDF resume against a target job role.
     * Enforces PDF file type and 2 MB max size (SRS REQ-RES-1, REQ-RES-2, REQ-RES-6).
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeAnalysisResponse> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "studentId", required = false, defaultValue = "1") Long studentId,
            @RequestParam(value = "targetRole", required = false, defaultValue = "Software Development Engineer (SDE)") String targetRole
    ) {
        ResumeAnalysisResponse response = resumeService.uploadAndAnalyzeResume(file, studentId, targetRole);
        return ResponseEntity.ok(response);
    }

    /**
     * Alias endpoint for upload (matches common frontend conventions).
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeAnalysisResponse> uploadAlias(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "studentId", required = false, defaultValue = "1") Long studentId,
            @RequestParam(value = "targetRole", required = false, defaultValue = "Software Development Engineer (SDE)") String targetRole
    ) {
        return uploadAndAnalyze(file, studentId, targetRole);
    }

    /**
     * Fetch analysis history of earlier attempts for a student (SRS REQ-RES-10).
     */
    @GetMapping("/history/{studentId}")
    public ResponseEntity<ResumeHistoryResponse> getStudentHistory(@PathVariable("studentId") Long studentId) {
        ResumeHistoryResponse response = resumeService.getStudentResumeHistory(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch latest resume analysis for a student (utilized by AI Interview Service REQ-INT-3).
     */
    @GetMapping("/latest/{studentId}")
    public ResponseEntity<ResumeAnalysisResponse> getLatestResume(@PathVariable("studentId") Long studentId) {
        ResumeAnalysisResponse response = resumeService.getLatestResumeAnalysis(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch single analysis result by resume ID.
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeAnalysisResponse> getResumeById(
            @PathVariable("resumeId") Long resumeId,
            @RequestParam(value = "studentId", required = false) Long studentId
    ) {
        ResumeAnalysisResponse response = resumeService.getResumeAnalysisById(resumeId, studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * List all supported target job roles and skill profiles (SRS TBD-14).
     */
    @GetMapping("/target-roles")
    public ResponseEntity<List<RoleSkillProfile>> getTargetRoles() {
        return ResponseEntity.ok(resumeService.getAvailableTargetRoles());
    }

    /**
     * Health and status check for service registry and gateway routing.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(resumeService.getServiceHealth());
    }
}
