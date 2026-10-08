package com.RoadMapService.RoadMapService.model;

public class WeakAreaItem {

    private String category;
    private String severity; // HIGH, MEDIUM, LOW
    private Double score;
    private String gapDescription;

    public WeakAreaItem() {
    }

    public WeakAreaItem(String category, String severity, Double score, String gapDescription) {
        this.category = category;
        this.severity = severity;
        this.score = score;
        this.gapDescription = gapDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getGapDescription() {
        return gapDescription;
    }

    public void setGapDescription(String gapDescription) {
        this.gapDescription = gapDescription;
    }
}
