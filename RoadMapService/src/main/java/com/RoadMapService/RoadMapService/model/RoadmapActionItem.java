package com.RoadMapService.RoadMapService.model;

public class RoadmapActionItem {

    private int stepNumber;
    private String title;
    private String category;
    private String priority; // HIGH, MEDIUM, LOW
    private String actionType; // PRACTICE_CODING, RETAKE_MOCK_INTERVIEW, UPDATE_RESUME, PRACTICE_APTITUDE, REVISE_CONCEPTS
    private String description;
    private int estimatedHours;
    private String suggestedTarget;
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED

    public RoadmapActionItem() {
    }

    public RoadmapActionItem(int stepNumber, String title, String category, String priority,
                             String actionType, String description, int estimatedHours,
                             String suggestedTarget, String status) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.actionType = actionType;
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.suggestedTarget = suggestedTarget;
        this.status = status != null ? status : "PENDING";
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getSuggestedTarget() {
        return suggestedTarget;
    }

    public void setSuggestedTarget(String suggestedTarget) {
        this.suggestedTarget = suggestedTarget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
