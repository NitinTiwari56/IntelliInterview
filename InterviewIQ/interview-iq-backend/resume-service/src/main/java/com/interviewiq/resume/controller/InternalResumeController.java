package com.interviewiq.resume.controller;

import com.interviewiq.resume.dto.ResumeAnalysisResponse;
import com.interviewiq.resume.dto.ResumeHistoryResponse;
import com.interviewiq.resume.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sirf doosri backend services ke liye (interview-service, analytics-service).
 * API Gateway mein /internal/** ka route nahi hai, isliye browser/frontend yahan nahi pahunch sakta.
 */
@RestController
@RequestMapping("/internal/resume")
public class InternalResumeController {

    private final ResumeService resumeService;

    public InternalResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /** Interview service: student ke resume (skills, projects, experience) se questions banane ke liye. */
    @GetMapping("/latest/{studentId}")
    public ResponseEntity<ResumeAnalysisResponse> latest(@PathVariable Long studentId) {
        return ResponseEntity.ok(resumeService.getLatestResumeAnalysis(studentId));
    }

    /** Analytics service: dashboard ke liye chhota summary (latest resume ka ATS score, role, skills). 404 = resume nahi hai. */
    @GetMapping("/{studentId}/summary")
    public ResponseEntity<ResumeSummary> summary(@PathVariable Long studentId) {
        ResumeAnalysisResponse r = resumeService.getLatestResumeAnalysis(studentId);
        return ResponseEntity.ok(new ResumeSummary(studentId, r.getAtsScore(), r.getTargetRole(),
                r.getExtractedSkills(), r.getMissingSkills()));
    }

    public record ResumeSummary(Long studentId, Integer atsScore, String targetRole,
                                List<String> extractedSkills, List<String> missingSkills) {}

    /** Analytics service: dashboard ke liye scores ki history. */
    @GetMapping("/history/{studentId}")
    public ResponseEntity<ResumeHistoryResponse> history(@PathVariable Long studentId) {
        return ResponseEntity.ok(resumeService.getStudentResumeHistory(studentId));
    }
}
