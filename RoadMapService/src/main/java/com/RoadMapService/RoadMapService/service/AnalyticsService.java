package com.RoadMapService.RoadMapService.service;

import com.RoadMapService.RoadMapService.dto.DashboardResponse;
import com.RoadMapService.RoadMapService.dto.RoadmapResponse;
import com.RoadMapService.RoadMapService.model.PerformanceProfileEntity;

public interface AnalyticsService {

    DashboardResponse getStudentDashboard(Long studentId);

    RoadmapResponse getStudentRoadmap(Long studentId);

    RoadmapResponse regenerateStudentRoadmap(Long studentId);

    PerformanceProfileEntity getOrCreatePerformanceProfile(Long studentId);

    void processModuleUpdateEvent(Long studentId, String category, Double score, String details);
}
