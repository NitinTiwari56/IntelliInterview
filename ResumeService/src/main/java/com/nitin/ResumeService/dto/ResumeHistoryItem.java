package com.nitin.ResumeService.dto;

import java.time.Instant;

public class ResumeHistoryItem {

    private Long resumeId;
    private String fileName;
    private String targetRole;
    private Integer atsScore;
    private Instant uploadedAt;

    public ResumeHistoryItem() {
    }

    public ResumeHistoryItem(Long resumeId, String fileName, String targetRole, Integer atsScore, Instant uploadedAt) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.targetRole = targetRole;
        this.atsScore = atsScore;
        this.uploadedAt = uploadedAt;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
