package com.RoadMapService.RoadMapService;

import com.RoadMapService.RoadMapService.client.CrossServiceClient;
import com.RoadMapService.RoadMapService.controller.DashboardController;
import com.RoadMapService.RoadMapService.controller.RoadmapController;
import com.RoadMapService.RoadMapService.dto.*;
import com.RoadMapService.RoadMapService.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RoadmapServiceIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private RoadmapController roadmapController;

    @MockitoBean
    private CrossServiceClient crossServiceClient;

    private MockMvc dashboardMockMvc;
    private MockMvc roadmapMockMvc;

    @BeforeEach
    void setUp() {
        this.dashboardMockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
        this.roadmapMockMvc = MockMvcBuilders.standaloneSetup(roadmapController).build();
    }

    @Test
    void testEmptyStateDashboardAndStarterRoadmap() {
        Long studentId = 999L;
        // Mock all external services returning empty
        when(crossServiceClient.fetchResumeSummary(studentId)).thenReturn(Optional.empty());
        when(crossServiceClient.fetchInterviewSummary(studentId)).thenReturn(Optional.empty());
        when(crossServiceClient.fetchCodingSummary(studentId)).thenReturn(Optional.empty());
        when(crossServiceClient.fetchAptitudeSummary(studentId)).thenReturn(Optional.empty());

        // 1. Dashboard empty state test (REQ-DSH-6)
        DashboardResponse dashboard = analyticsService.getStudentDashboard(studentId);
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.isHasData()).isFalse();
        assertThat(dashboard.getReadinessLevel()).isEqualTo("Getting Started");

        // 2. Roadmap generic starter test (REQ-RMP-7)
        RoadmapResponse roadmap = analyticsService.getStudentRoadmap(studentId);
        assertThat(roadmap).isNotNull();
        assertThat(roadmap.isGenericStarter()).isTrue();
        assertThat(roadmap.getRecommendedActions()).isNotEmpty();
        assertThat(roadmap.getRecommendedActions()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void testAggregatedDashboardAndPersonalizedRoadmap() {
        Long studentId = 101L;

        // Mock Resume Service response
        ResumeSummaryDto resume = new ResumeSummaryDto();
        resume.setStudentId(studentId);
        resume.setAtsScore(82);
        resume.setTargetRole("Java Backend Developer");
        resume.setExtractedSkills(List.of("Java", "Spring Boot", "MySQL"));
        resume.setMissingSkills(List.of("Kafka", "Docker", "System Design"));
        when(crossServiceClient.fetchResumeSummary(studentId)).thenReturn(Optional.of(resume));

        // Mock Interview Service response
        InterviewSummaryDto interview = new InterviewSummaryDto();
        interview.setStudentId(studentId);
        interview.setTotalSessions(2);
        interview.setCompletedSessions(2);
        interview.setAverageTechnicalScore(62.0); // weak area (< 75)
        interview.setAverageHrScore(80.0);
        interview.setOverallAverageScore(71.0);
        when(crossServiceClient.fetchInterviewSummary(studentId)).thenReturn(Optional.of(interview));

        // Mock Coding Service response
        CodingSummaryDto coding = new CodingSummaryDto();
        coding.setStudentId(studentId);
        coding.setProblemsSolved(8);
        coding.setAverageScore(75.0);
        when(crossServiceClient.fetchCodingSummary(studentId)).thenReturn(Optional.of(coding));

        // Mock Aptitude Service response
        AptitudeSummaryDto aptitude = new AptitudeSummaryDto();
        aptitude.setStudentId(studentId);
        aptitude.setOverallScore(70.0);
        when(crossServiceClient.fetchAptitudeSummary(studentId)).thenReturn(Optional.of(aptitude));

        // 1. Verify Dashboard aggregation
        DashboardResponse dashboard = analyticsService.getStudentDashboard(studentId);
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.isHasData()).isTrue();
        assertThat(dashboard.getOverallReadinessScore()).isGreaterThan(0.0);
        assertThat(dashboard.getCategoryChartData()).isNotEmpty();
        assertThat(dashboard.getWeakAreas()).isNotEmpty();

        // Check that Technical Interview was identified as weak area (score 62.0 < 75.0)
        boolean hasTechWeakness = dashboard.getWeakAreas().stream()
                .anyMatch(w -> "TECHNICAL_INTERVIEW".equals(w.getCategory()));
        assertThat(hasTechWeakness).isTrue();

        // 2. Verify Roadmap generation
        RoadmapResponse roadmap = analyticsService.regenerateStudentRoadmap(studentId);
        assertThat(roadmap).isNotNull();
        assertThat(roadmap.isGenericStarter()).isFalse();
        assertThat(roadmap.getRecommendedActions()).isNotEmpty();
        assertThat(roadmap.getTargetRole()).isEqualTo("Java Backend Developer");
    }

    @Test
    void testRestControllers() throws Exception {
        Long studentId = 101L;

        // Test Roadmap Health
        roadmapMockMvc.perform(get("/api/roadmap/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("RoadMapService"));

        // Test Dashboard GET
        dashboardMockMvc.perform(get("/api/dashboard/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(101));

        // Test Roadmap GET
        roadmapMockMvc.perform(get("/api/roadmap/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(101))
                .andExpect(jsonPath("$.recommendedActions").isArray());

        // Test Roadmap POST regenerate
        roadmapMockMvc.perform(post("/api/roadmap/" + studentId + "/regenerate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(101));
    }
}
