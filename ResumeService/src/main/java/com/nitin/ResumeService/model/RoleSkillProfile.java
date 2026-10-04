package com.nitin.ResumeService.model;

import java.util.List;

public class RoleSkillProfile {
    private String roleName;
    private String description;
    private List<String> coreSkills;
    private List<String> secondarySkills;
    private List<String> toolsAndFrameworks;

    public RoleSkillProfile() {
    }

    public RoleSkillProfile(String roleName, String description, List<String> coreSkills,
                            List<String> secondarySkills, List<String> toolsAndFrameworks) {
        this.roleName = roleName;
        this.description = description;
        this.coreSkills = coreSkills;
        this.secondarySkills = secondarySkills;
        this.toolsAndFrameworks = toolsAndFrameworks;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCoreSkills() {
        return coreSkills;
    }

    public void setCoreSkills(List<String> coreSkills) {
        this.coreSkills = coreSkills;
    }

    public List<String> getSecondarySkills() {
        return secondarySkills;
    }

    public void setSecondarySkills(List<String> secondarySkills) {
        this.secondarySkills = secondarySkills;
    }

    public List<String> getToolsAndFrameworks() {
        return toolsAndFrameworks;
    }

    public void setToolsAndFrameworks(List<String> toolsAndFrameworks) {
        this.toolsAndFrameworks = toolsAndFrameworks;
    }
}
