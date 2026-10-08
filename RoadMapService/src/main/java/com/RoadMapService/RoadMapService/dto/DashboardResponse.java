package com.RoadMapService.RoadMapService.dto;

import com.RoadMapService.RoadMapService.model.WeakAreaItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private Long studentId;
    private String targetRole;
    private boolean hasData;
    private Double overallReadinessScore; // 0-100 (REQ-DSH-3 / TBD-8)
    private String readinessLevel; // "Placement Ready", "Needs Improvement", "Getting Started"
    private Map<String, Object> moduleScores = new HashMap<>();
    private List<ChartSliceDto> categoryChartData = new ArrayList<>(); // REQ-DSH-4
    private List<WeakAreaItem> weakAreas = new ArrayList<>();
    private List<String> strengths = new ArrayList<>();
    private Map<String, String> serviceStatus = new HashMap<>(); // REQ-DSH-7 resilience

    public DashboardResponse() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public boolean isHasData() { return hasData; }
    public void setHasData(boolean hasData) { this.hasData = hasData; }

    public Double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(Double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }

    public String getReadinessLevel() { return readinessLevel; }
    public void setReadinessLevel(String readinessLevel) { this.readinessLevel = readinessLevel; }

    public Map<String, Object> getModuleScores() { return moduleScores; }
    public void setModuleScores(Map<String, Object> moduleScores) { this.moduleScores = moduleScores; }

    public List<ChartSliceDto> getCategoryChartData() { return categoryChartData; }
    public void setCategoryChartData(List<ChartSliceDto> categoryChartData) { this.categoryChartData = categoryChartData != null ? categoryChartData : new ArrayList<>(); }

    public List<WeakAreaItem> getWeakAreas() { return weakAreas; }
    public void setWeakAreas(List<WeakAreaItem> weakAreas) { this.weakAreas = weakAreas != null ? weakAreas : new ArrayList<>(); }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths != null ? strengths : new ArrayList<>(); }

    public Map<String, String> getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(Map<String, String> serviceStatus) { this.serviceStatus = serviceStatus; }
}
