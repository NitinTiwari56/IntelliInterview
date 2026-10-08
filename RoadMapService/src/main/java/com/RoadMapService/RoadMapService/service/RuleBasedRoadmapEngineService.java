package com.RoadMapService.RoadMapService.service;

import com.RoadMapService.RoadMapService.dto.RoadmapResponse;
import com.RoadMapService.RoadMapService.model.PerformanceProfileEntity;
import com.RoadMapService.RoadMapService.model.RoadmapActionItem;
import com.RoadMapService.RoadMapService.model.WeakAreaItem;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleBasedRoadmapEngineService {

    public RoadmapResponse generateGenericStarterRoadmap(Long studentId, String targetRole) {
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : "Software Development Engineer";

        RoadmapResponse resp = new RoadmapResponse();
        resp.setStudentId(studentId);
        resp.setTargetRole(role);
        resp.setGenericStarter(true);
        resp.setOverallReadinessScore(50.0);
        resp.setSummaryVerdict("Welcome to Interview IQ! Complete your baseline assessments to generate your customized AI weakness roadmap.");
        resp.setGeneratedAt(Instant.now());

        List<RoadmapActionItem> actions = new ArrayList<>();
        actions.add(new RoadmapActionItem(
                1,
                "Upload Resume & Target Role for ATS Gap Analysis",
                "RESUME",
                "HIGH",
                "UPDATE_RESUME",
                "Upload your latest PDF resume targeting " + role + " to receive immediate ATS keyword gap scoring.",
                1,
                "Target ATS Score: 80%+",
                "PENDING"
        ));
        actions.add(new RoadmapActionItem(
                2,
                "Attempt 1 Technical Mock Interview",
                "TECHNICAL_INTERVIEW",
                "HIGH",
                "RETAKE_MOCK_INTERVIEW",
                "Complete a 5-question AI Technical Interview to evaluate core language fundamentals and architecture skills.",
                2,
                "Target Score: 75%+",
                "PENDING"
        ));
        actions.add(new RoadmapActionItem(
                3,
                "Solve 3 Core DSA Coding Problems",
                "CODING",
                "HIGH",
                "PRACTICE_CODING",
                "Practice Arrays, HashMaps, and Two-Pointer problems in the coding sandbox.",
                3,
                "Pass all test cases",
                "PENDING"
        ));
        actions.add(new RoadmapActionItem(
                4,
                "Take a 10-Question Timed Aptitude Drill",
                "APTITUDE",
                "MEDIUM",
                "PRACTICE_APTITUDE",
                "Practice speed and accuracy across Quantitative and Logical aptitude questions with a 30-second timer.",
                1,
                "Score 70%+",
                "PENDING"
        ));
        actions.add(new RoadmapActionItem(
                5,
                "Take 1 HR Mock Behavioral Interview",
                "HR_INTERVIEW",
                "MEDIUM",
                "RETAKE_MOCK_INTERVIEW",
                "Practice answering common situational questions using the STAR framework.",
                1,
                "Score 80%+",
                "PENDING"
        ));

        resp.setRecommendedActions(actions);
        resp.setTotalEstimatedHours(actions.stream().mapToInt(RoadmapActionItem::getEstimatedHours).sum());

        List<WeakAreaItem> defaultWeakAreas = List.of(
                new WeakAreaItem("BASELINE_ASSESSMENT", "MEDIUM", 50.0, "Complete module activities to establish accurate baseline data.")
        );
        resp.setIdentifiedWeakAreas(defaultWeakAreas);

        return resp;
    }

    public RoadmapResponse generatePersonalizedRoadmap(Long studentId, String targetRole,
                                                       PerformanceProfileEntity profile,
                                                       List<String> missingSkills) {
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : "Software Development Engineer";

        RoadmapResponse resp = new RoadmapResponse();
        resp.setStudentId(studentId);
        resp.setTargetRole(role);
        resp.setGenericStarter(false);
        resp.setOverallReadinessScore(profile.getOverallReadinessScore());
        resp.setGeneratedAt(Instant.now());

        List<WeakAreaItem> weakAreas = profile.getWeakAreas();
        resp.setIdentifiedWeakAreas(weakAreas);

        List<RoadmapActionItem> actions = new ArrayList<>();
        int step = 1;

        // Prioritize actions based on weak areas
        for (WeakAreaItem weak : weakAreas) {
            if ("HIGH".equalsIgnoreCase(weak.getSeverity()) || "MEDIUM".equalsIgnoreCase(weak.getSeverity())) {
                switch (weak.getCategory()) {
                    case "RESUME" -> {
                        String desc = (missingSkills != null && !missingSkills.isEmpty()) ?
                                "Incorporate critical missing keywords into your projects: " + String.join(", ", missingSkills.stream().limit(4).toList()) :
                                "Restructure bullet points with quantifiable action verbs and metrics.";
                        actions.add(new RoadmapActionItem(
                                step++,
                                "Optimize Resume Keywords for " + role,
                                "RESUME",
                                "HIGH",
                                "UPDATE_RESUME",
                                desc,
                                2,
                                "Achieve ATS Score > 80%",
                                "PENDING"
                        ));
                    }
                    case "TECHNICAL_INTERVIEW" -> actions.add(new RoadmapActionItem(
                            step++,
                            "Retake Technical Mock Interview (Focus on Architectural Trade-offs)",
                            "TECHNICAL_INTERVIEW",
                            "HIGH",
                            "RETAKE_MOCK_INTERVIEW",
                            "Review question feedback and re-attempt technical mock interview focusing on concurrency and internal mechanisms.",
                            3,
                            "Score > 75%",
                            "PENDING"
                    ));
                    case "HR_INTERVIEW" -> actions.add(new RoadmapActionItem(
                            step++,
                            "Refine Behavioral Answers using STAR Method",
                            "HR_INTERVIEW",
                            "MEDIUM",
                            "RETAKE_MOCK_INTERVIEW",
                            "Formulate structured stories for conflict resolution, deadlines, and project roadblocks.",
                            2,
                            "Score > 80%",
                            "PENDING"
                    ));
                    case "CODING" -> actions.add(new RoadmapActionItem(
                            step++,
                            "Solve 5 Intermediate DSA Coding Problems",
                            "CODING",
                            "HIGH",
                            "PRACTICE_CODING",
                            "Practice problems in Trees, Dynamic Programming, and Graph Traversals.",
                            4,
                            "100% Test Case Pass Rate",
                            "PENDING"
                    ));
                    case "APTITUDE" -> actions.add(new RoadmapActionItem(
                            step++,
                            "Daily 15-Minute Aptitude Drills",
                            "APTITUDE",
                            "MEDIUM",
                            "PRACTICE_APTITUDE",
                            "Practice speed calculation for Percentages, Profit/Loss, and Syllogisms within 30-second timers.",
                            2,
                            "Accuracy > 75%",
                            "PENDING"
                    ));
                }
            }
        }

        // If candidate performed well across all, give advanced polish items
        if (actions.isEmpty()) {
            actions.add(new RoadmapActionItem(
                    step++,
                    "Deep Dive System Design & High-Concurrency Patterns",
                    "ADVANCED",
                    "MEDIUM",
                    "REVISE_CONCEPTS",
                    "Study distributed caching (Redis), message queues (Kafka), and database sharding patterns.",
                    3,
                    "Read top engineering blogs",
                    "PENDING"
            ));
            actions.add(new RoadmapActionItem(
                    step++,
                    "Maintain Interview Readiness with Full-Length Mock Rounds",
                    "INTERVIEW",
                    "LOW",
                    "RETAKE_MOCK_INTERVIEW",
                    "Conduct weekly refresher mock interviews to keep presentation sharp.",
                    2,
                    "Consistent 85%+ score",
                    "PENDING"
            ));
        }

        resp.setRecommendedActions(actions);
        resp.setTotalEstimatedHours(actions.stream().mapToInt(RoadmapActionItem::getEstimatedHours).sum());

        double readiness = profile.getOverallReadinessScore() != null ? profile.getOverallReadinessScore() : 60.0;
        if (readiness >= 80.0) {
            resp.setSummaryVerdict("Candidate demonstrates strong placement readiness across multiple evaluation dimensions. Follow prioritized refinement steps to maximize offer conversion.");
        } else if (readiness >= 65.0) {
            resp.setSummaryVerdict("Solid foundation established. Focusing effort on the identified high-priority weakness categories will yield the fastest readiness gains.");
        } else {
            resp.setSummaryVerdict("Foundational preparation phase. Complete the targeted remedial steps in the roadmap sequentially to build confidence.");
        }

        return resp;
    }
}
