package com.nitin.ResumeService.service;

import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import com.nitin.ResumeService.dto.ResumeAnalyzedEvent;
import com.nitin.ResumeService.dto.ResumeHistoryItem;
import com.nitin.ResumeService.dto.ResumeHistoryResponse;
import com.nitin.ResumeService.exception.ResourceNotFoundException;
import com.nitin.ResumeService.model.ResumeEntity;
import com.nitin.ResumeService.model.RoleSkillProfile;
import com.nitin.ResumeService.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final PdfParserService pdfParserService;
    private final FileStorageService fileStorageService;
    private final AiResumeAnalysisService aiResumeAnalysisService;
    private final ResumeRepository resumeRepository;
    private final ResumeEventPublisher eventPublisher;
    private final TargetRoleService targetRoleService;

    public ResumeServiceImpl(PdfParserService pdfParserService,
                             FileStorageService fileStorageService,
                             AiResumeAnalysisService aiResumeAnalysisService,
                             ResumeRepository resumeRepository,
                             ResumeEventPublisher eventPublisher,
                             TargetRoleService targetRoleService) {
        this.pdfParserService = pdfParserService;
        this.fileStorageService = fileStorageService;
        this.aiResumeAnalysisService = aiResumeAnalysisService;
        this.resumeRepository = resumeRepository;
        this.eventPublisher = eventPublisher;
        this.targetRoleService = targetRoleService;
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse uploadAndAnalyzeResume(MultipartFile file, Long studentId, String targetRole) {
        if (studentId == null || studentId <= 0) {
            studentId = 1L; // Default demo studentId
        }

        if (targetRole == null || targetRole.trim().isEmpty()) {
            targetRole = "Software Development Engineer (SDE)";
        }

        log.info("Starting resume analysis for Student ID {} with target role '{}'...", studentId, targetRole);

        // 1. Validate & Parse PDF (REQ-RES-2, REQ-RES-3)
        String resumeText = pdfParserService.extractTextFromPdf(file);

        // 2. Safely Store Uploaded PDF file to disk
        String storedFilePath = fileStorageService.storeFile(file, studentId);

        // 3. AI / Rule-Based Resume Analysis (REQ-RES-4, REQ-RES-5, REQ-RES-6)
        ResumeAnalysisResponse analysis = aiResumeAnalysisService.analyzeResume(resumeText, targetRole);

        // 4. Persist to Database (resumes table, REQ-RES-7)
        ResumeEntity entity = new ResumeEntity();
        entity.setStudentId(studentId);
        entity.setFilePath(storedFilePath);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileSize(file.getSize());
        entity.setTargetRole(analysis.getTargetRole());
        entity.setAtsScore(analysis.getAtsScore());
        entity.setFormatScore(analysis.getFormatScore());
        entity.setExperienceScore(analysis.getExperienceScore());
        entity.setExtractedSkills(analysis.getExtractedSkills());
        entity.setMatchedSkills(analysis.getMatchedSkills());
        entity.setMissingSkills(analysis.getMissingSkills());
        entity.setSuggestions(analysis.getSuggestions());
        entity.setSummaryFeedback(analysis.getSummaryFeedback());
        entity.setAnalysisSource(analysis.getAnalysisSource());
        entity.setExtractedData(analysis.getExtractedData());
        entity.setUploadedAt(Instant.now());

        ResumeEntity savedEntity = resumeRepository.save(entity);
        log.info("Saved resume analysis record with ID: {}", savedEntity.getResumeId());

        // 5. Populate response IDs and metadata
        analysis.setResumeId(savedEntity.getResumeId());
        analysis.setStudentId(savedEntity.getStudentId());
        analysis.setFileName(savedEntity.getFileName());
        analysis.setFileSize(savedEntity.getFileSize());
        analysis.setUploadedAt(savedEntity.getUploadedAt());

        // 6. Publish ResumeAnalyzed Event for Analytics Service (REQ-RES-8)
        ResumeAnalyzedEvent event = new ResumeAnalyzedEvent(
                savedEntity.getStudentId(),
                savedEntity.getResumeId(),
                savedEntity.getTargetRole(),
                savedEntity.getAtsScore(),
                savedEntity.getFormatScore(),
                savedEntity.getExperienceScore(),
                savedEntity.getExtractedSkills(),
                savedEntity.getMissingSkills()
        );
        eventPublisher.publishResumeAnalyzedEvent(event);

        return analysis;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeHistoryResponse getStudentResumeHistory(Long studentId) {
        if (studentId == null || studentId <= 0) {
            studentId = 1L;
        }

        List<ResumeEntity> list = resumeRepository.findByStudentIdOrderByUploadedAtDesc(studentId);
        List<ResumeHistoryItem> historyItems = new ArrayList<>();

        double sumScore = 0.0;
        Integer latestScore = null;

        for (int i = 0; i < list.size(); i++) {
            ResumeEntity r = list.get(i);
            if (i == 0) {
                latestScore = r.getAtsScore();
            }
            sumScore += r.getAtsScore();
            historyItems.add(new ResumeHistoryItem(
                    r.getResumeId(),
                    r.getFileName(),
                    r.getTargetRole(),
                    r.getAtsScore(),
                    r.getUploadedAt()
            ));
        }

        Double avgScore = list.isEmpty() ? 0.0 : Math.round((sumScore / list.size()) * 10.0) / 10.0;

        return new ResumeHistoryResponse(studentId, list.size(), avgScore, latestScore, historyItems);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getResumeAnalysisById(Long resumeId, Long studentId) {
        Optional<ResumeEntity> opt;
        if (studentId != null && studentId > 0) {
            opt = resumeRepository.findByResumeIdAndStudentId(resumeId, studentId);
        } else {
            opt = resumeRepository.findById(resumeId);
        }

        ResumeEntity entity = opt.orElseThrow(() ->
                new ResourceNotFoundException("Resume analysis record not found with ID: " + resumeId));

        return mapEntityToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getLatestResumeAnalysis(Long studentId) {
        final Long effectiveStudentId = (studentId != null && studentId > 0) ? studentId : 1L;

        ResumeEntity entity = resumeRepository.findTopByStudentIdOrderByUploadedAtDesc(effectiveStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("No previous resume analysis found for Student ID: " + effectiveStudentId));

        return mapEntityToResponse(entity);
    }

    @Override
    public List<RoleSkillProfile> getAvailableTargetRoles() {
        return targetRoleService.getAllRoles();
    }

    @Override
    public Map<String, Object> getServiceHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "ResumeService");
        health.put("totalResumesStored", resumeRepository.count());
        health.put("aiConfigured", aiResumeAnalysisService.isAiConfigured());
        health.put("timestamp", Instant.now().toString());
        health.put("supportedRolesCount", targetRoleService.getAllRoles().size());
        return health;
    }

    private ResumeAnalysisResponse mapEntityToResponse(ResumeEntity entity) {
        ResumeAnalysisResponse res = new ResumeAnalysisResponse();
        res.setResumeId(entity.getResumeId());
        res.setStudentId(entity.getStudentId());
        res.setFileName(entity.getFileName());
        res.setFileSize(entity.getFileSize());
        res.setTargetRole(entity.getTargetRole());
        res.setAtsScore(entity.getAtsScore());
        res.setFormatScore(entity.getFormatScore());
        res.setExperienceScore(entity.getExperienceScore());
        res.setExtractedSkills(entity.getExtractedSkills());
        res.setMatchedSkills(entity.getMatchedSkills());
        res.setMissingSkills(entity.getMissingSkills());
        res.setSuggestions(entity.getSuggestions());
        res.setSummaryFeedback(entity.getSummaryFeedback());
        res.setAnalysisSource(entity.getAnalysisSource());
        res.setExtractedData(entity.getExtractedData());
        res.setUploadedAt(entity.getUploadedAt());
        return res;
    }
}
