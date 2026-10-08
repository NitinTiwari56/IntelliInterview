package com.RoadMapService.RoadMapService.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmap", indexes = {
        @Index(name = "idx_roadmap_student_id", columnList = "student_id", unique = true)
})
public class RoadmapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long roadmapId;

    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    @Column(name = "target_role", length = 150)
    private String targetRole;

    @Column(name = "is_generic_starter")
    private Boolean isGenericStarter = false;

    @Convert(converter = RoadmapActionListJsonConverter.class)
    @Column(name = "recommended_actions", columnDefinition = "TEXT")
    private List<RoadmapActionItem> recommendedActions = new ArrayList<>();

    @Column(name = "summary_verdict", columnDefinition = "TEXT")
    private String summaryVerdict;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public RoadmapEntity() {
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.generatedAt == null) {
            this.generatedAt = Instant.now();
        }
    }

    public Long getRoadmapId() {
        return roadmapId;
    }

    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Boolean getIsGenericStarter() {
        return isGenericStarter;
    }

    public void setIsGenericStarter(Boolean isGenericStarter) {
        this.isGenericStarter = isGenericStarter;
    }

    public List<RoadmapActionItem> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<RoadmapActionItem> recommendedActions) {
        this.recommendedActions = recommendedActions != null ? recommendedActions : new ArrayList<>();
    }

    public String getSummaryVerdict() {
        return summaryVerdict;
    }

    public void setSummaryVerdict(String summaryVerdict) {
        this.summaryVerdict = summaryVerdict;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
