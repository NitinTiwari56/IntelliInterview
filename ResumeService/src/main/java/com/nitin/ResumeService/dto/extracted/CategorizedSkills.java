package com.nitin.ResumeService.dto.extracted;

import java.util.ArrayList;
import java.util.List;

public class CategorizedSkills {
    private List<String> languages = new ArrayList<>();
    private List<String> frameworks = new ArrayList<>();
    private List<String> toolsAndDatabases = new ArrayList<>();
    private List<String> coreSubjects = new ArrayList<>();

    public CategorizedSkills() {
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages != null ? languages : new ArrayList<>();
    }

    public List<String> getFrameworks() {
        return frameworks;
    }

    public void setFrameworks(List<String> frameworks) {
        this.frameworks = frameworks != null ? frameworks : new ArrayList<>();
    }

    public List<String> getToolsAndDatabases() {
        return toolsAndDatabases;
    }

    public void setToolsAndDatabases(List<String> toolsAndDatabases) {
        this.toolsAndDatabases = toolsAndDatabases != null ? toolsAndDatabases : new ArrayList<>();
    }

    public List<String> getCoreSubjects() {
        return coreSubjects;
    }

    public void setCoreSubjects(List<String> coreSubjects) {
        this.coreSubjects = coreSubjects != null ? coreSubjects : new ArrayList<>();
    }
}
