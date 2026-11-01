package com.interviewiq.resume.service;

import com.interviewiq.resume.dto.ResumeAnalysisResponse;
import com.interviewiq.resume.model.ResumeEntity;

public final class ResumeMapper {

    private ResumeMapper() {}

    public static ResumeAnalysisResponse toResponse(ResumeEntity entity) {
        ResumeAnalysisResponse res = new ResumeAnalysisResponse();
        res.setResumeId(entity.getResumeId());
        res.setStudentId(entity.getStudentId());
        res.setFileName(entity.getFileName());
        res.setFileSize(entity.getFileSize());
        res.setTargetRole(entity.getTargetRole());
        res.setAtsScore(entity.getAtsScore());
        res.setFormatScore(entity.getFormatScore());
        res.setExperienceScore(entity.getExperienceScore());
        res.setSkillMatchScore(entity.getSkillMatchScore());
        res.setExtractedSkills(entity.getExtractedSkills());
        res.setMatchedSkills(entity.getMatchedSkills());
        res.setMissingSkills(entity.getMissingSkills());
        res.setSuggestions(entity.getSuggestions());
        res.setSummaryFeedback(entity.getSummaryFeedback());
        res.setAnalysisSource(entity.getAnalysisSource());
        res.setExtractedData(entity.getExtractedData());
        res.setUploadedAt(entity.getUploadedAt());
        res.setLastEditedAt(entity.getLastEditedAt());
        return res;
    }
}
