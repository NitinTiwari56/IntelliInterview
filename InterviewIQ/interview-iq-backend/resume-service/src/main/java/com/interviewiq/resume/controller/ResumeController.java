package com.interviewiq.resume.controller;

import com.interviewiq.resume.dto.ResumeAnalysisResponse;
import com.interviewiq.resume.dto.ResumeDetailsUpdateRequest;
import com.interviewiq.resume.dto.RoadmapResponse;
import com.interviewiq.resume.dto.ResumeHistoryResponse;
import com.interviewiq.resume.model.RoleSkillProfile;
import com.interviewiq.resume.service.ResumeProfileService;
import com.interviewiq.resume.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Student-facing APIs (gateway ke through: /resume/**).
 * User ki pehchaan SIRF gateway ke "X-User-Id" header se aati hai (verified JWT se).
 * Request mein studentId kabhi accept nahi hota, warna koi bhi doosre ka resume dekh leta.
 */
@RestController
@RequestMapping("/resume")
public class ResumeController {

    private static final String DEFAULT_ROLE = "Software Development Engineer (SDE)";

    private final ResumeService resumeService;
    private final ResumeProfileService profileService;

    public ResumeController(ResumeService resumeService, ResumeProfileService profileService) {
        this.resumeService = resumeService;
        this.profileService = profileService;
    }

    /** Upload and analyze a PDF resume against a target job role (SRS REQ-RES-1, 2, 6). */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeAnalysisResponse> analyze(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetRole", required = false, defaultValue = DEFAULT_ROLE) String targetRole) {
        return ResponseEntity.ok(resumeService.uploadAndAnalyzeResume(file, userId, targetRole));
    }

    /** Meri purani analyses ki history (SRS REQ-RES-10). */
    @GetMapping("/history")
    public ResponseEntity<ResumeHistoryResponse> history(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(resumeService.getStudentResumeHistory(userId));
    }

    /**
     * Resume ki details direct edit karo (latest analysis row update hoti hai).
     * Jo field null/missing hai wo waisi rehti hai. Response mein naya skillMatchScore, matched/missing skills.
     */
    @PutMapping("/details")
    public ResponseEntity<ResumeAnalysisResponse> updateDetails(@RequestHeader("X-User-Id") Long userId,
                                                                @RequestBody ResumeDetailsUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateDetails(userId, request));
    }

    /** Roadmap = target role ki skills ka checklist (done/baki), priority ke saath. Edit ke baad update hota hai. */
    @GetMapping("/roadmap")
    public ResponseEntity<RoadmapResponse> roadmap(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.roadmap(userId));
    }

    /** Meri sabse latest analysis. */
    @GetMapping("/latest")
    public ResponseEntity<ResumeAnalysisResponse> latest(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(resumeService.getLatestResumeAnalysis(userId));
    }

    @GetMapping("/target-roles")
    public ResponseEntity<List<RoleSkillProfile>> targetRoles() {
        return ResponseEntity.ok(resumeService.getAvailableTargetRoles());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(resumeService.getServiceHealth());
    }

    /** Meri koi ek analysis (id se). Doosre ki id dene pe 404. */
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeAnalysisResponse> byId(@RequestHeader("X-User-Id") Long userId,
                                                       @PathVariable("resumeId") Long resumeId) {
        return ResponseEntity.ok(resumeService.getResumeAnalysisById(resumeId, userId));
    }
}
