package com.RoadMapService.RoadMapService.dto;

public class ChartSliceDto {

    private String category;
    private Double score;
    private Double normalizedScore;
    private String color;

    public ChartSliceDto() {
    }

    public ChartSliceDto(String category, Double score, Double normalizedScore, String color) {
        this.category = category;
        this.score = score;
        this.normalizedScore = normalizedScore;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getNormalizedScore() {
        return normalizedScore;
    }

    public void setNormalizedScore(Double normalizedScore) {
        this.normalizedScore = normalizedScore;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
