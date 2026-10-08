package com.RoadMapService.RoadMapService.service;

import com.RoadMapService.RoadMapService.client.CrossServiceClient;
import com.RoadMapService.RoadMapService.dto.*;
import com.RoadMapService.RoadMapService.model.*;
import com.RoadMapService.RoadMapService.repository.PerformanceProfileRepository;
import com.RoadMapService.RoadMapService.repository.RoadmapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final PerformanceProfileRepository profileRepository;
    private final RoadmapRepository roadmapRepository;
    private final CrossServiceClient crossServiceClient;
    private final AiRoadmapEngineService aiRoadmapEngine;
    private final RuleBasedRoadmapEngineService ruleBasedRoadmapEngine;
    private final RoadmapEventPublisher eventPublisher;

    public AnalyticsServiceImpl(PerformanceProfileRepository profileRepository,
                                RoadmapRepository roadmapRepository,
                                CrossServiceClient crossServiceClient,
                                AiRoadmapEngineService aiRoadmapEngine,
                                RuleBasedRoadmapEngineService ruleBasedRoadmapEngine,
                                RoadmapEventPublisher eventPublisher) {
        this.profileRepository = profileRepository;
        this.roadmapRepository = roadmapRepository;
        this.crossServiceClient = crossServiceClient;
        this.aiRoadmapEngine = aiRoadmapEngine;
        this.ruleBasedRoadmapEngine = ruleBasedRoadmapEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DashboardResponse getStudentDashboard(Long studentId) {
        log.info("Aggregating cross-service dashboard data for student ID: {}", studentId);

        DashboardResponse resp = new DashboardResponse();
        resp.setStudentId(studentId);

        // 1. Fetch synchronous data from peer services (Figure B-6)
        Optional<ResumeSummaryDto> resumeOpt = crossServiceClient.fetchResumeSummary(studentId);
        Optional<InterviewSummaryDto> interviewOpt = crossServiceClient.fetchInterviewSummary(studentId);
        Optional<CodingSummaryDto> codingOpt = crossServiceClient.fetchCodingSummary(studentId);
        Optional<AptitudeSummaryDto> aptitudeOpt = crossServiceClient.fetchAptitudeSummary(studentId);

        // Record service availability
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("ResumeService", resumeOpt.isPresent() ? "AVAILABLE" : "UNAVAILABLE_OR_EMPTY");
        statusMap.put("InterviewService", interviewOpt.isPresent() ? "AVAILABLE" : "UNAVAILABLE_OR_EMPTY");
        statusMap.put("CodingService", codingOpt.isPresent() ? "AVAILABLE" : "UNAVAILABLE_OR_EMPTY");
        statusMap.put("AptitudeService", aptitudeOpt.isPresent() ? "AVAILABLE" : "UNAVAILABLE_OR_EMPTY");
        resp.setServiceStatus(statusMap);

        // Extract module scores
        Double resumeScore = resumeOpt.map(r -> r.getAtsScore() != null ? (double) r.getAtsScore() : null).orElse(null);
        Double techScore = interviewOpt.map(InterviewSummaryDto::getAverageTechnicalScore).orElse(null);
        Double hrScore = interviewOpt.map(InterviewSummaryDto::getAverageHrScore).orElse(null);
        Double codingScore = codingOpt.map(CodingSummaryDto::getAverageScore).orElse(null);
        Double aptScore = aptitudeOpt.map(AptitudeSummaryDto::getOverallScore).orElse(null);

        String targetRole = resumeOpt.map(ResumeSummaryDto::getTargetRole).orElse("Software Development Engineer");
        resp.setTargetRole(targetRole);

        // Check if student has data in any module
        boolean hasData = (resumeScore != null || techScore != null || hrScore != null || codingScore != null || aptScore != null);
        resp.setHasData(hasData);

        // Map detailed module scores
        Map<String, Object> moduleScores = new HashMap<>();
        moduleScores.put("resumeAtsScore", resumeScore);
        moduleScores.put("technicalInterviewScore", techScore);
        moduleScores.put("hrInterviewScore", hrScore);
        moduleScores.put("codingScore", codingScore);
        moduleScores.put("codingProblemsSolved", codingOpt.map(CodingSummaryDto::getProblemsSolved).orElse(0));
        moduleScores.put("aptitudeScore", aptScore);

        if (aptitudeOpt.isPresent()) {
            Map<String, Double> aptBreakdown = new HashMap<>();
            aptBreakdown.put("quant", aptitudeOpt.get().getQuantScore());
            aptBreakdown.put("logical", aptitudeOpt.get().getLogicalScore());
            aptBreakdown.put("verbal", aptitudeOpt.get().getVerbalScore());
            moduleScores.put("aptitudeBreakdown", aptBreakdown);
        }
        resp.setModuleScores(moduleScores);

        // Calculate Overall Readiness Score (TBD-8: weighted average formula)
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        if (resumeScore != null) { totalWeightedScore += resumeScore * 0.20; totalWeight += 0.20; }
        if (techScore != null)   { totalWeightedScore += techScore * 0.30; totalWeight += 0.30; }
        if (hrScore != null)     { totalWeightedScore += hrScore * 0.15; totalWeight += 0.15; }
        if (codingScore != null) { totalWeightedScore += codingScore * 0.20; totalWeight += 0.20; }
        if (aptScore != null)    { totalWeightedScore += aptScore * 0.15; totalWeight += 0.15; }

        double overallReadiness = totalWeight > 0.0 ? Math.round((totalWeightedScore / totalWeight) * 10.0) / 10.0 : 50.0;
        resp.setOverallReadinessScore(overallReadiness);

        if (overallReadiness >= 80.0) {
            resp.setReadinessLevel("Placement Ready");
        } else if (overallReadiness >= 65.0) {
            resp.setReadinessLevel("Needs Targeted Improvement");
        } else {
            resp.setReadinessLevel("Getting Started");
        }

        // Generate Chart Data Slices (REQ-DSH-4)
        List<ChartSliceDto> chartData = new ArrayList<>();
        if (resumeScore != null) chartData.add(new ChartSliceDto("Resume ATS", resumeScore, resumeScore, "#4F46E5"));
        if (techScore != null) chartData.add(new ChartSliceDto("Technical Interview", techScore, techScore, "#06B6D4"));
        if (hrScore != null) chartData.add(new ChartSliceDto("HR Interview", hrScore, hrScore, "#10B981"));
        if (codingScore != null) chartData.add(new ChartSliceDto("DSA Coding", codingScore, codingScore, "#F59E0B"));
        if (aptScore != null) chartData.add(new ChartSliceDto("Aptitude", aptScore, aptScore, "#EC4899"));
        resp.setCategoryChartData(chartData);

        // Detect Weak Areas & Strengths (REQ-RMP-2 / TBD-9)
        List<WeakAreaItem> weakAreas = new ArrayList<>();
        List<String> strengths = new ArrayList<>();

        evaluateCategoryWeakness("RESUME", resumeScore, 75.0,
                "Resume ATS score below 75%. Critical technical keywords or project impact metrics are missing.",
                "Strong resume profile matching target industry roles.",
                weakAreas, strengths);

        evaluateCategoryWeakness("TECHNICAL_INTERVIEW", techScore, 75.0,
                "Technical interview fundamentals require deeper explanation of internal mechanisms and concurrency.",
                "Excellent technical articulation and conceptual foundation.",
                weakAreas, strengths);

        evaluateCategoryWeakness("HR_INTERVIEW", hrScore, 70.0,
                "HR behavioral answers lack structured storytelling (STAR method) and quantifiable outcomes.",
                "Confident communication and structured responses in HR mock rounds.",
                weakAreas, strengths);

        evaluateCategoryWeakness("CODING", codingScore, 70.0,
                "DSA coding accuracy and test case passing rate need practice in medium/hard problems.",
                "Solid algorithmic problem solving and clean implementation.",
                weakAreas, strengths);

        evaluateCategoryWeakness("APTITUDE", aptScore, 70.0,
                "Timed aptitude test score is below target threshold; speed and calculation accuracy need improvement.",
                "Fast, accurate problem solving in quantitative and logical aptitude.",
                weakAreas, strengths);

        resp.setWeakAreas(weakAreas);
        resp.setStrengths(strengths);

        // Upsert Performance Profile (SRS Table 6-1 / REQ-RMP-3)
        PerformanceProfileEntity profile = profileRepository.findByStudentId(studentId)
                .orElse(new PerformanceProfileEntity());
        profile.setStudentId(studentId);
        profile.setOverallReadinessScore(overallReadiness);
        profile.setResumeScore(resumeScore);
        profile.setTechnicalInterviewScore(techScore);
        profile.setHrInterviewScore(hrScore);
        profile.setCodingScore(codingScore);
        profile.setAptitudeScore(aptScore);
        profile.setWeakAreas(weakAreas);
        profile.setStrengths(strengths);
        profile.setLastUpdated(Instant.now());
        profileRepository.save(profile);

        return resp;
    }

    private void evaluateCategoryWeakness(String category, Double score, double threshold,
                                         String weakDesc, String strengthDesc,
                                         List<WeakAreaItem> weakAreas, List<String> strengths) {
        if (score == null) return;
        if (score < 60.0) {
            weakAreas.add(new WeakAreaItem(category, "HIGH", score, weakDesc));
        } else if (score < threshold) {
            weakAreas.add(new WeakAreaItem(category, "MEDIUM", score, weakDesc));
        } else {
            strengths.add(strengthDesc);
        }
    }

    @Override
    @Transactional
    public RoadmapResponse getStudentRoadmap(Long studentId) {
        // Check if roadmap is already cached and recent
        Optional<RoadmapEntity> existing = roadmapRepository.findByStudentId(studentId);
        if (existing.isPresent()) {
            RoadmapEntity entity = existing.get();
            RoadmapResponse resp = new RoadmapResponse();
            resp.setStudentId(entity.getStudentId());
            resp.setTargetRole(entity.getTargetRole());
            resp.setGenericStarter(Boolean.TRUE.equals(entity.getIsGenericStarter()));
            resp.setSummaryVerdict(entity.getSummaryVerdict());
            resp.setRecommendedActions(entity.getRecommendedActions());
            resp.setTotalEstimatedHours(entity.getRecommendedActions().stream().mapToInt(RoadmapActionItem::getEstimatedHours).sum());
            resp.setGeneratedAt(entity.getGeneratedAt());

            profileRepository.findByStudentId(studentId).ifPresent(p -> {
                resp.setOverallReadinessScore(p.getOverallReadinessScore());
                resp.setIdentifiedWeakAreas(p.getWeakAreas());
            });
            return resp;
        }

        return regenerateStudentRoadmap(studentId);
    }

    @Override
    @Transactional
    public RoadmapResponse regenerateStudentRoadmap(Long studentId) {
        log.info("Generating personalized roadmap for student ID: {}", studentId);

        // 1. Gather fresh cross-service aggregate and update profile
        DashboardResponse dashboard = getStudentDashboard(studentId);
        PerformanceProfileEntity profile = profileRepository.findByStudentId(studentId).orElseThrow();

        // 2. Fetch missing skills if available from resume service
        Optional<ResumeSummaryDto> resumeOpt = crossServiceClient.fetchResumeSummary(studentId);
        List<String> missingSkills = resumeOpt.map(ResumeSummaryDto::getMissingSkills).orElse(Collections.emptyList());
        String targetRole = dashboard.getTargetRole();

        RoadmapResponse roadmapResponse;

        // 3. REQ-RMP-7: If student has no data, show generic starter roadmap
        if (!dashboard.isHasData()) {
            roadmapResponse = ruleBasedRoadmapEngine.generateGenericStarterRoadmap(studentId, targetRole);
        } else {
            // Generate customized roadmap via AI or Rule-Based Engine
            roadmapResponse = aiRoadmapEngine.generateRoadmap(studentId, targetRole, profile, missingSkills);
        }

        // 4. Upsert into roadmap table (SRS Table 6-1 / REQ-RMP-5)
        RoadmapEntity entity = roadmapRepository.findByStudentId(studentId)
                .orElse(new RoadmapEntity());
        entity.setStudentId(studentId);
        entity.setTargetRole(targetRole);
        entity.setIsGenericStarter(roadmapResponse.isGenericStarter());
        entity.setRecommendedActions(roadmapResponse.getRecommendedActions());
        entity.setSummaryVerdict(roadmapResponse.getSummaryVerdict());
        entity.setGeneratedAt(Instant.now());
        RoadmapEntity saved = roadmapRepository.save(entity);

        // 5. Publish RoadmapUpdated event for Notification Service (Table 3-4 / REQ-RMP-8)
        List<String> topActions = roadmapResponse.getRecommendedActions().stream()
                .map(RoadmapActionItem::getTitle)
                .limit(3)
                .toList();

        RoadmapUpdatedEvent event = new RoadmapUpdatedEvent(
                studentId,
                saved.getRoadmapId(),
                targetRole,
                roadmapResponse.getOverallReadinessScore(),
                roadmapResponse.getRecommendedActions().size(),
                topActions
        );
        eventPublisher.publishRoadmapUpdatedEvent(event);

        return roadmapResponse;
    }

    @Override
    public PerformanceProfileEntity getOrCreatePerformanceProfile(Long studentId) {
        return profileRepository.findByStudentId(studentId).orElseGet(() -> {
            PerformanceProfileEntity p = new PerformanceProfileEntity();
            p.setStudentId(studentId);
            p.setOverallReadinessScore(50.0);
            p.setLastUpdated(Instant.now());
            return profileRepository.save(p);
        });
    }

    @Override
    @Transactional
    public void processModuleUpdateEvent(Long studentId, String category, Double score, String details) {
        log.info("[ASYNC SIGNAL] Processing module update event for Student ID {}. Category: {}, Score: {}",
                studentId, category, score);
        // Automatically regenerate roadmap upon receiving new completion signals
        regenerateStudentRoadmap(studentId);
    }
}
