package com.RoadMapService.RoadMapService.dto;

import com.RoadMapService.RoadMapService.model.RoadmapActionItem;
import com.RoadMapService.RoadMapService.model.WeakAreaItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RoadmapResponse {

    private Long studentId;
    private String targetRole;
    private boolean isGenericStarter;
    private Double overallReadinessScore;
    private String summaryVerdict;
    private int totalEstimatedHours;
    private List<RoadmapActionItem> recommendedActions = new ArrayList<>();
    private List<WeakAreaItem> identifiedWeakAreas = new ArrayList<>();
    private Instant generatedAt;

    public RoadmapResponse() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public boolean isGenericStarter() { return isGenericStarter; }
    public void setGenericStarter(boolean genericStarter) { isGenericStarter = genericStarter; }

    public Double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(Double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }

    public String getSummaryVerdict() { return summaryVerdict; }
    public void setSummaryVerdict(String summaryVerdict) { this.summaryVerdict = summaryVerdict; }

    public int getTotalEstimatedHours() { return totalEstimatedHours; }
    public void setTotalEstimatedHours(int totalEstimatedHours) { this.totalEstimatedHours = totalEstimatedHours; }

    public List<RoadmapActionItem> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<RoadmapActionItem> recommendedActions) { this.recommendedActions = recommendedActions != null ? recommendedActions : new ArrayList<>(); }

    public List<WeakAreaItem> getIdentifiedWeakAreas() { return identifiedWeakAreas; }
    public void setIdentifiedWeakAreas(List<WeakAreaItem> identifiedWeakAreas) { this.identifiedWeakAreas = identifiedWeakAreas != null ? identifiedWeakAreas : new ArrayList<>(); }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
