package com.nitin.ResumeService.dto.extracted;

import java.util.ArrayList;
import java.util.List;

public class ExperienceItem {
    private String company;
    private String role;
    private String duration;
    private List<String> highlights = new ArrayList<>();

    public ExperienceItem() {
    }

    public ExperienceItem(String company, String role, String duration, List<String> highlights) {
        this.company = company;
        this.role = role;
        this.duration = duration;
        this.highlights = highlights != null ? highlights : new ArrayList<>();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights != null ? highlights : new ArrayList<>();
    }
}
