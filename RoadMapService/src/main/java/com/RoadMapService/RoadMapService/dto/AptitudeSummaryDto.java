package com.RoadMapService.RoadMapService.dto;

public class AptitudeSummaryDto {

    private Long studentId;
    private int totalAttempted = 0;
    private Double overallScore = 0.0;
    private Double quantScore = 0.0;
    private Double logicalScore = 0.0;
    private Double verbalScore = 0.0;

    public AptitudeSummaryDto() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public Double getQuantScore() { return quantScore; }
    public void setQuantScore(Double quantScore) { this.quantScore = quantScore; }

    public Double getLogicalScore() { return logicalScore; }
    public void setLogicalScore(Double logicalScore) { this.logicalScore = logicalScore; }

    public Double getVerbalScore() { return verbalScore; }
    public void setVerbalScore(Double verbalScore) { this.verbalScore = verbalScore; }
}
