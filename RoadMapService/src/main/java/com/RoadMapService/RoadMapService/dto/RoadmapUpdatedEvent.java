package com.RoadMapService.RoadMapService.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RoadmapUpdatedEvent {

    private String eventType = "RoadmapUpdated";
    private Long studentId;
    private Long roadmapId;
    private String targetRole;
    private Double overallReadinessScore;
    private int actionCount;
    private List<String> topPriorityActions = new ArrayList<>();
    private Instant timestamp;

    public RoadmapUpdatedEvent() {
        this.timestamp = Instant.now();
    }

    public RoadmapUpdatedEvent(Long studentId, Long roadmapId, String targetRole,
                               Double overallReadinessScore, int actionCount,
                               List<String> topPriorityActions) {
        this.eventType = "RoadmapUpdated";
        this.studentId = studentId;
        this.roadmapId = roadmapId;
        this.targetRole = targetRole;
        this.overallReadinessScore = overallReadinessScore;
        this.actionCount = actionCount;
        this.topPriorityActions = topPriorityActions != null ? topPriorityActions : new ArrayList<>();
        this.timestamp = Instant.now();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getRoadmapId() { return roadmapId; }
    public void setRoadmapId(Long roadmapId) { this.roadmapId = roadmapId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(Double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }

    public int getActionCount() { return actionCount; }
    public void setActionCount(int actionCount) { this.actionCount = actionCount; }

    public List<String> getTopPriorityActions() { return topPriorityActions; }
    public void setTopPriorityActions(List<String> topPriorityActions) { this.topPriorityActions = topPriorityActions; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
