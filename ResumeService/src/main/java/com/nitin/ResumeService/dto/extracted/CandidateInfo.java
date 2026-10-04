package com.nitin.ResumeService.dto.extracted;

import java.util.ArrayList;
import java.util.List;

public class CandidateInfo {
    private String name;
    private String email;
    private String phone;
    private String linkedin;
    private String github;
    private String portfolio;

    public CandidateInfo() {
    }

    public CandidateInfo(String name, String email, String phone, String linkedin, String github, String portfolio) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.linkedin = linkedin;
        this.github = github;
        this.portfolio = portfolio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }
}
