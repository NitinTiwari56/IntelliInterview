package com.interviewiq.coding.service;

import com.interviewiq.coding.dto.CodingDtos.*;
import com.interviewiq.coding.entity.Difficulty;
import com.interviewiq.coding.entity.Problem;
import com.interviewiq.coding.entity.TestCase;
import com.interviewiq.coding.entity.Verdict;
import com.interviewiq.coding.exception.ApiException;
import com.interviewiq.coding.repository.ProblemRepository;
import com.interviewiq.coding.repository.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public ProblemService(ProblemRepository problemRepository, SubmissionRepository submissionRepository) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    // ===================== student =====================

    @Transactional(readOnly = true)
    public List<ProblemListItem> list(Long userId, Difficulty difficulty, String topic) {
        Map<Long, Long> acceptedByProblem = acceptedCounts(userId);   // problemId -> accepted count

        List<ProblemListItem> items = new ArrayList<>();
        for (Problem p : problemRepository.findByPublishedTrueOrderByIdAsc()) {
            if (difficulty != null && p.getDifficulty() != difficulty) {
                continue;
            }
            if (topic != null && !topic.isBlank() && !p.getTopic().equalsIgnoreCase(topic.trim())) {
                continue;
            }
            String status = "TODO";
            if (acceptedByProblem.containsKey(p.getId())) {
                status = acceptedByProblem.get(p.getId()) > 0 ? "SOLVED" : "ATTEMPTED";
            }
            items.add(new ProblemListItem(p.getId(), p.getSlug(), p.getTitle(), p.getDifficulty(), p.getTopic(), status));
        }
        return items;
    }

    @Transactional(readOnly = true)
    public ProblemDetail detail(String slug) {
        Problem p = getPublishedWithCases(slug);
        List<SampleCase> samples = p.getTestCases().stream()
                .filter(TestCase::isSample)
                .map(tc -> new SampleCase(tc.getInput(), tc.getExpectedOutput()))
                .toList();
        return new ProblemDetail(p.getId(), p.getSlug(), p.getTitle(), p.getDescription(), p.getDifficulty(),
                p.getTopic(), p.getTimeLimitSeconds(), p.getMemoryLimitKb(), samples);
    }

    /** Test cases (hidden bhi) ke saath. Sirf server ke andar use hota hai. */
    @Transactional(readOnly = true)
    public Problem getPublishedWithCases(String slug) {
        return problemRepository.findWithTestCasesBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Problem not found"));
    }

    /** problemId -> accepted submissions ki ginti, un sab problems ke liye jo user ne try ki hain. */
    public Map<Long, Long> acceptedCounts(Long userId) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : submissionRepository.problemSummary(userId, Verdict.ACCEPTED)) {
            result.put((Long) row[0], row[1] == null ? 0L : ((Number) row[1]).longValue());
        }
        return result;
    }

    // ===================== admin =====================

    @Transactional(readOnly = true)
    public List<AdminProblemSummary> adminList() {
        return problemRepository.findAllByOrderByIdAsc().stream()
                .map(p -> new AdminProblemSummary(p.getId(), p.getSlug(), p.getTitle(), p.getDifficulty(),
                        p.getTopic(), p.isPublished(), p.getTestCases().size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProblemResponse adminGet(Long id) {
        return toAdminResponse(findForAdmin(id));
    }

    @Transactional
    public AdminProblemResponse create(ProblemUpsertRequest request) {
        if (problemRepository.existsBySlug(request.slug())) {
            throw new ApiException(HttpStatus.CONFLICT, "A problem with this slug already exists");
        }
        validateCases(request);
        Problem p = new Problem();
        apply(p, request);
        return toAdminResponse(problemRepository.save(p));
    }

    /** Pura replace: test cases bhi naye bhej do. (Unpublish karne ke liye published=false bhejo.) */
    @Transactional
    public AdminProblemResponse update(Long id, ProblemUpsertRequest request) {
        Problem p = findForAdmin(id);
        if (!p.getSlug().equals(request.slug()) && problemRepository.existsBySlug(request.slug())) {
            throw new ApiException(HttpStatus.CONFLICT, "A problem with this slug already exists");
        }
        validateCases(request);
        apply(p, request);
        return toAdminResponse(p);
    }

    /**
     * Bulk import. Poora import ek transaction mein hai: ek bhi problem galat ho toh KUCH save nahi hota.
     * Same slug = update (test cases replace), naya slug = create.
     */
    @Transactional
    public ImportResponse importAll(ImportRequest request) {
        Set<String> seen = new HashSet<>();
        List<String> createdSlugs = new ArrayList<>();
        List<String> updatedSlugs = new ArrayList<>();

        for (ProblemUpsertRequest r : request.problems()) {
            if (!seen.add(r.slug())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate slug in import: " + r.slug());
            }
            try {
                validateCases(r);
            } catch (ApiException e) {
                throw new ApiException(e.getStatus(), r.slug() + ": " + e.getMessage());
            }
            Optional<Problem> existing = problemRepository.findWithTestCasesBySlug(r.slug());
            if (existing.isPresent()) {
                apply(existing.get(), r);
                updatedSlugs.add(r.slug());
            } else {
                Problem p = new Problem();
                apply(p, r);
                problemRepository.save(p);
                createdSlugs.add(r.slug());
            }
        }
        return new ImportResponse(createdSlugs.size(), updatedSlugs.size(), createdSlugs, updatedSlugs);
    }

    private Problem findForAdmin(Long id) {
        return problemRepository.findWithTestCasesById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Problem not found"));
    }

    private void validateCases(ProblemUpsertRequest request) {
        if (request.testCases().stream().noneMatch(TestCaseRequest::sample)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one test case must be marked as sample");
        }
    }

    private void apply(Problem p, ProblemUpsertRequest r) {
        p.setSlug(r.slug());
        p.setTitle(r.title().trim());
        p.setDescription(r.description());
        p.setDifficulty(r.difficulty());
        p.setTopic(r.topic().trim());
        p.setTimeLimitSeconds(r.timeLimitSeconds() == null ? 2.0 : r.timeLimitSeconds());
        p.setMemoryLimitKb(r.memoryLimitKb() == null ? 256000 : r.memoryLimitKb());
        p.setPublished(r.published() == null || r.published());

        p.getTestCases().clear();      // orphanRemoval purane cases delete kar deta hai
        int position = 1;
        for (TestCaseRequest tcr : r.testCases()) {
            TestCase tc = new TestCase();
            tc.setProblem(p);
            tc.setPosition(position++);
            tc.setInput(tcr.input());
            tc.setExpectedOutput(tcr.expectedOutput());
            tc.setSample(tcr.sample());
            p.getTestCases().add(tc);
        }
    }

    private AdminProblemResponse toAdminResponse(Problem p) {
        List<AdminTestCase> cases = p.getTestCases().stream()
                .map(tc -> new AdminTestCase(tc.getId(), tc.getPosition(), tc.getInput(),
                        tc.getExpectedOutput(), tc.isSample()))
                .toList();
        return new AdminProblemResponse(p.getId(), p.getSlug(), p.getTitle(), p.getDescription(),
                p.getDifficulty(), p.getTopic(), p.getTimeLimitSeconds(), p.getMemoryLimitKb(),
                p.isPublished(), cases);
    }
}
