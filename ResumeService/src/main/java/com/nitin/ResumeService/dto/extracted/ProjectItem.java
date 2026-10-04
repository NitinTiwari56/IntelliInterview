package com.nitin.ResumeService.dto.extracted;

import java.util.ArrayList;
import java.util.List;

public class ProjectItem {
    private String title;
    private List<String> techStack = new ArrayList<>();
    private String description;
    private String link;

    public ProjectItem() {
    }

    public ProjectItem(String title, List<String> techStack, String description, String link) {
        this.title = title;
        this.techStack = techStack != null ? techStack : new ArrayList<>();
        this.description = description;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public void setTechStack(List<String> techStack) {
        this.techStack = techStack != null ? techStack : new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
