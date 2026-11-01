package com.interviewiq.analytics.service;

import com.interviewiq.analytics.dto.DashboardResponse;
import com.interviewiq.analytics.dto.RoadmapResponse;
import com.interviewiq.analytics.model.PerformanceProfileEntity;

public interface AnalyticsService {

    DashboardResponse getStudentDashboard(Long studentId);

    RoadmapResponse getStudentRoadmap(Long studentId);

    RoadmapResponse regenerateStudentRoadmap(Long studentId);

    PerformanceProfileEntity getOrCreatePerformanceProfile(Long studentId);

    void processModuleUpdateEvent(Long studentId, String category, Double score, String details);
}
