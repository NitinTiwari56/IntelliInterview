package com.interviewiq.resume.dto;

import java.time.Instant;
import java.util.List;

public record RoadmapResponse(Long resumeId, String targetRole, int skillMatchScore, int coveredCount,
                              int totalCount, Integer atsScore, Instant lastEditedAt, List<RoadmapItem> items) {

    /** priority: CORE | SECONDARY | TOOL. done=true = resume mein ye skill hai. Pehle baki (undone) items, priority ke order mein. */
    public record RoadmapItem(String skill, String priority, boolean done, String hint) {}
}
