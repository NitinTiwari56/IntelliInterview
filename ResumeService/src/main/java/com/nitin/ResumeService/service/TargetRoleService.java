package com.nitin.ResumeService.service;

import com.nitin.ResumeService.model.RoleSkillProfile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TargetRoleService {

    private final Map<String, RoleSkillProfile> roleCatalog = new LinkedHashMap<>();

    public TargetRoleService() {
        initRoles();
    }

    private void initRoles() {
        registerRole(new RoleSkillProfile(
                "Software Development Engineer (SDE)",
                "Focuses on core algorithms, object-oriented design, clean code, scalable architecture, and problem solving.",
                List.of("Data Structures", "Algorithms", "Java", "C++", "Python", "Object Oriented Programming (OOP)", "Database Management Systems (DBMS)", "SQL", "Operating Systems", "Computer Networks"),
                List.of("System Design", "Git", "REST APIs", "Multithreading", "Spring Boot", "Design Patterns", "Unit Testing"),
                List.of("Git", "Postman", "Linux", "Docker", "JUnit", "Maven")
        ));

        registerRole(new RoleSkillProfile(
                "Backend Engineer",
                "Specializes in server-side logic, database architecture, API design, scalability, and distributed systems.",
                List.of("Java", "Python", "Node.js", "Spring Boot", "REST APIs", "SQL", "PostgreSQL", "MySQL", "Database Design", "Microservices"),
                List.of("Redis", "Apache Kafka", "Docker", "Kubernetes", "Authentication (JWT/OAuth)", "NoSQL (MongoDB)", "Caching", "GraphQL", "CI/CD"),
                List.of("Git", "Docker", "Postman", "Maven/Gradle", "AWS", "JPA/Hibernate")
        ));

        registerRole(new RoleSkillProfile(
                "Frontend Engineer",
                "Builds modern, responsive, high-performance web applications and delightful user interfaces.",
                List.of("JavaScript", "TypeScript", "React.js", "HTML5", "CSS3", "Responsive Web Design", "DOM Manipulation", "State Management (Redux/Zustand)"),
                List.of("Next.js", "Tailwind CSS", "REST APIs", "Webpack/Vite", "WebSockets", "Web Performance Optimization", "Accessibility (a11y)", "Jest/Testing Library"),
                List.of("Git", "npm/yarn", "Chrome DevTools", "Figma", "Postman")
        ));

        registerRole(new RoleSkillProfile(
                "Full Stack Engineer",
                "End-to-end web application development encompassing frontend, backend, databases, and deployment.",
                List.of("React.js", "JavaScript/TypeScript", "Node.js/Express", "Java/Spring Boot", "SQL (PostgreSQL/MySQL)", "RESTful APIs", "HTML5/CSS3"),
                List.of("MongoDB", "Docker", "Authentication (JWT)", "State Management", "Microservices", "Git", "Cloud Deployment (AWS/Azure)"),
                List.of("Git", "Docker", "Postman", "VS Code", "Vite", "npm")
        ));

        registerRole(new RoleSkillProfile(
                "Data Scientist / Machine Learning Engineer",
                "Designs predictive models, processes data pipelines, and deploys machine learning and deep learning solutions.",
                List.of("Python", "Machine Learning", "Data Analysis", "SQL", "Pandas", "NumPy", "Scikit-Learn", "Probability & Statistics"),
                List.of("Deep Learning", "TensorFlow/PyTorch", "Data Visualization (Matplotlib/Seaborn)", "Feature Engineering", "NLP", "LLMs", "Model Deployment (FastAPI/Flask)"),
                List.of("Jupyter Notebook", "Git", "Docker", "Hugging Face", "MLflow")
        ));

        registerRole(new RoleSkillProfile(
                "DevOps & Cloud Engineer",
                "Manages cloud infrastructure, continuous integration and delivery pipelines, and system reliability.",
                List.of("Linux/Shell Scripting", "Docker", "Kubernetes", "CI/CD (GitHub Actions/Jenkins)", "AWS / Cloud Platforms", "Git", "Terraform / Infrastructure as Code"),
                List.of("Ansible", "Prometheus & Grafana", "Networking & Security", "Python / Go", "Nginx", "Kubernetes Helm"),
                List.of("Docker", "Kubernetes", "Terraform", "GitHub Actions", "AWS Console", "Bash")
        ));

        registerRole(new RoleSkillProfile(
                "Mobile App Developer (Android/iOS)",
                "Builds native and cross-platform mobile apps with smooth performance and offline capability.",
                List.of("Kotlin", "Java", "Swift", "Flutter / Dart", "React Native", "Android SDK / iOS SDK", "Mobile UI Design"),
                List.of("REST API Integration", "SQLite/Room", "Firebase", "State Management", "Push Notifications", "App Store / Play Store Deployment"),
                List.of("Android Studio", "Xcode", "Git", "Postman", "Firebase")
        ));

        registerRole(new RoleSkillProfile(
                "QA & Test Automation Engineer",
                "Ensures software quality through automated test frameworks, API validation, and performance tests.",
                List.of("Java / Python", "Selenium WebDriver", "Test Automation", "Manual Testing", "API Testing (Postman/RestAssured)", "SQL", "JUnit / TestNG"),
                List.of("CI/CD Test Integration", "Cypress / Playwright", "Performance Testing (JMeter)", "BDD / Cucumber", "Bug Tracking (Jira)"),
                List.of("Selenium", "Postman", "Git", "Jira", "Jenkins")
        ));
    }

    private void registerRole(RoleSkillProfile profile) {
        roleCatalog.put(normalizeKey(profile.getRoleName()), profile);
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        return key.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public List<RoleSkillProfile> getAllRoles() {
        return new ArrayList<>(roleCatalog.values());
    }

    public RoleSkillProfile getRoleProfile(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return roleCatalog.get(normalizeKey("Software Development Engineer (SDE)"));
        }

        String searchKey = normalizeKey(roleName);
        if (roleCatalog.containsKey(searchKey)) {
            return roleCatalog.get(searchKey);
        }

        // Fuzzy match: check for keywords (e.g. "sde", "frontend", "backend", "fullstack", "devops", "machine learning")
        for (Map.Entry<String, RoleSkillProfile> entry : roleCatalog.entrySet()) {
            if (searchKey.contains("sde") || searchKey.contains("software")) {
                if (entry.getKey().contains("sde")) return entry.getValue();
            }
            if (searchKey.contains("front") && entry.getKey().contains("front")) return entry.getValue();
            if (searchKey.contains("back") && entry.getKey().contains("back")) return entry.getValue();
            if (searchKey.contains("full") && entry.getKey().contains("full")) return entry.getValue();
            if ((searchKey.contains("data") || searchKey.contains("ml") || searchKey.contains("machine")) && entry.getKey().contains("data")) return entry.getValue();
            if (searchKey.contains("devop") && entry.getKey().contains("devop")) return entry.getValue();
            if (searchKey.contains("mobile") && entry.getKey().contains("mobile")) return entry.getValue();
            if (searchKey.contains("qa") || searchKey.contains("test")) {
                if (entry.getKey().contains("qa")) return entry.getValue();
            }
        }

        // Default to SDE profile if unrecognized
        return roleCatalog.get(normalizeKey("Software Development Engineer (SDE)"));
    }
}
