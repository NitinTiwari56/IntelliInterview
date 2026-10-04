package com.nitin.ResumeService.dto.extracted;

public class EducationItem {
    private String institution;
    private String degree;
    private String year;
    private String score;

    public EducationItem() {
    }

    public EducationItem(String institution, String degree, String year, String score) {
        this.institution = institution;
        this.degree = degree;
        this.year = year;
        this.score = score;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
