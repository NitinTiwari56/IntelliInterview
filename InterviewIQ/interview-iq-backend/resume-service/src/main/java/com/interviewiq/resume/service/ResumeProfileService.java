package com.interviewiq.resume.service;

import com.interviewiq.resume.dto.ResumeAnalysisResponse;
import com.interviewiq.resume.dto.ResumeDetailsUpdateRequest;
import com.interviewiq.resume.dto.RoadmapResponse;
import com.interviewiq.resume.dto.RoadmapResponse.RoadmapItem;
import com.interviewiq.resume.dto.extracted.*;
import com.interviewiq.resume.exception.ResourceNotFoundException;
import com.interviewiq.resume.model.ResumeEntity;
import com.interviewiq.resume.repository.ResumeRepository;
import com.interviewiq.resume.service.SkillGapCalculator.Item;
import com.interviewiq.resume.service.SkillGapCalculator.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * "Direct update": student ki sabse latest resume row (extracted_data) ko seedha edit karta hai
 * aur skill match / roadmap turant dobara nikalta hai. ATS score PDF se aaya tha, wo nahi badalta.
 */
@Service
public class ResumeProfileService {

    private static final Logger log = LoggerFactory.getLogger(ResumeProfileService.class);

    private static final int MAX_SKILLS_PER_LIST = 60;
    private static final int MAX_ITEMS = 20;
    private static final int MAX_CERTIFICATIONS = 30;
    private static final int MAX_TEXT = 2000;

    private final ResumeRepository resumeRepository;
    private final SkillGapCalculator calculator;
    private final TargetRoleService targetRoleService;

    public ResumeProfileService(ResumeRepository resumeRepository, SkillGapCalculator calculator,
                                TargetRoleService targetRoleService) {
        this.resumeRepository = resumeRepository;
        this.calculator = calculator;
        this.targetRoleService = targetRoleService;
    }

    @Transactional
    public ResumeAnalysisResponse updateDetails(Long userId, ResumeDetailsUpdateRequest req) {
        ResumeEntity entity = latestOrThrow(userId);
        validate(req);

        ExtractedResumeData old = entity.getExtractedData() != null ? entity.getExtractedData() : new ExtractedResumeData();

        // NAYA object banate hain (purane ko modify nahi), taaki JPA ko change pakad mein aaye
        ExtractedResumeData updated = new ExtractedResumeData();
        updated.setCandidateInfo(req.candidateInfo() != null ? req.candidateInfo() : old.getCandidateInfo());
        updated.setEducation(req.education() != null ? withoutNulls(req.education()) : old.getEducation());
        updated.setExperience(req.experience() != null ? withoutNulls(req.experience()) : old.getExperience());
        updated.setProjects(req.projects() != null ? withoutNulls(req.projects()) : old.getProjects());
        updated.setSkills(req.skills() != null ? cleanSkills(req.skills()) : old.getSkills());
        updated.setCertifications(req.certifications() != null ? cleanList(req.certifications()) : old.getCertifications());
        updated.setRawText(old.getRawText());                    // PDF ka original text same rehta hai

        if (req.targetRole() != null && !req.targetRole().isBlank()) {
            entity.setTargetRole(targetRoleService.getRoleProfile(req.targetRole()).getRoleName());
        }

        Result result = calculator.compute(updated, entity.getTargetRole());

        entity.setExtractedData(updated);
        entity.setExtractedSkills(result.userSkills());
        entity.setMatchedSkills(result.matched());
        entity.setMissingSkills(result.missing());
        entity.setSkillMatchScore(result.score());
        entity.setLastEditedAt(Instant.now());

        ResumeEntity saved = resumeRepository.save(entity);
        log.info("Resume {} of user {} edited. Skill match score now {}", saved.getResumeId(), userId, result.score());
        return ResumeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoadmapResponse roadmap(Long userId) {
        ResumeEntity entity = latestOrThrow(userId);
        Result result = calculator.compute(entity.getExtractedData(), entity.getTargetRole());

        List<Item> sorted = new ArrayList<>(result.items());
        sorted.sort(Comparator.comparing(Item::done).thenComparingInt(i -> priorityOrder(i.priority())));

        List<RoadmapItem> items = sorted.stream()
                .map(i -> new RoadmapItem(i.skill(), i.priority(), i.done(), hint(i)))
                .toList();

        return new RoadmapResponse(entity.getResumeId(), entity.getTargetRole(), result.score(), result.covered(),
                result.total(), entity.getAtsScore(), entity.getLastEditedAt(), items);
    }

    // ---------------------------------------------------------------------

    private ResumeEntity latestOrThrow(Long userId) {
        return resumeRepository.findTopByStudentIdOrderByUploadedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload a resume first to edit your details."));
    }

    private int priorityOrder(String priority) {
        return switch (priority) {
            case "CORE" -> 0;
            case "SECONDARY" -> 1;
            default -> 2;
        };
    }

    private String hint(Item i) {
        if (i.done()) {
            return "Covered in your resume.";
        }
        return switch (i.priority()) {
            case "CORE" -> "Core skill for this role. Learn it, add it to your Skills, and prove it in a project.";
            case "SECONDARY" -> "Good to have for this role. Add it once you have used it in a project or practice.";
            default -> "Tool or framework recruiters look for. Try it in a small hands-on project.";
        };
    }

    private void validate(ResumeDetailsUpdateRequest req) {
        check(req.education(), MAX_ITEMS, "education entries");
        check(req.experience(), MAX_ITEMS, "experience entries");
        check(req.projects(), MAX_ITEMS, "projects");
        check(req.certifications(), MAX_CERTIFICATIONS, "certifications");
        if (req.skills() != null) {
            CategorizedSkills s = req.skills();
            check(s.getLanguages(), MAX_SKILLS_PER_LIST, "languages");
            check(s.getFrameworks(), MAX_SKILLS_PER_LIST, "frameworks");
            check(s.getToolsAndDatabases(), MAX_SKILLS_PER_LIST, "tools");
            check(s.getCoreSubjects(), MAX_SKILLS_PER_LIST, "core subjects");
        }
        if (req.projects() != null) {
            for (ProjectItem p : req.projects()) {
                if (p != null && p.getDescription() != null && p.getDescription().length() > MAX_TEXT) {
                    throw new IllegalArgumentException("Project description is too long (max " + MAX_TEXT + " characters)");
                }
            }
        }
    }

    private void check(List<?> list, int max, String what) {
        if (list != null && list.size() > max) {
            throw new IllegalArgumentException("Too many " + what + " (max " + max + ")");
        }
    }

    private <T> List<T> withoutNulls(List<T> list) {
        List<T> out = new ArrayList<>();
        for (T item : list) {
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    private CategorizedSkills cleanSkills(CategorizedSkills in) {
        CategorizedSkills out = new CategorizedSkills();
        out.setLanguages(cleanList(in.getLanguages()));
        out.setFrameworks(cleanList(in.getFrameworks()));
        out.setToolsAndDatabases(cleanList(in.getToolsAndDatabases()));
        out.setCoreSubjects(cleanList(in.getCoreSubjects()));
        return out;
    }

    /** trim, khali hatao, duplicates (case-insensitive) hatao. */
    private List<String> cleanList(List<String> in) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String s : in) {
            if (s == null) {
                continue;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > 100) {
                t = t.substring(0, 100);
            }
            unique.putIfAbsent(t.toLowerCase(Locale.ROOT), t);
        }
        return new ArrayList<>(unique.values());
    }
}
