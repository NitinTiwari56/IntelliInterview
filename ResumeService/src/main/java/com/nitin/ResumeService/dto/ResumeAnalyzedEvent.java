package com.nitin.ResumeService.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ResumeAnalyzedEvent {

    private String eventType = "ResumeAnalyzed";
    private Long studentId;
    private Long resumeId;
    private String targetRole;
    private Integer atsScore;
    private Integer formatScore;
    private Integer experienceScore;
    private List<String> extractedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private Instant timestamp;

    public ResumeAnalyzedEvent() {
        this.timestamp = Instant.now();
    }

    public ResumeAnalyzedEvent(Long studentId, Long resumeId, String targetRole, Integer atsScore,
                               Integer formatScore, Integer experienceScore, List<String> extractedSkills,
                               List<String> missingSkills) {
        this.eventType = "ResumeAnalyzed";
        this.studentId = studentId;
        this.resumeId = resumeId;
        this.targetRole = targetRole;
        this.atsScore = atsScore;
        this.formatScore = formatScore;
        this.experienceScore = experienceScore;
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
        this.timestamp = Instant.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public Integer getFormatScore() {
        return formatScore;
    }

    public void setFormatScore(Integer formatScore) {
        this.formatScore = formatScore;
    }

    public Integer getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(Integer experienceScore) {
        this.experienceScore = experienceScore;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
