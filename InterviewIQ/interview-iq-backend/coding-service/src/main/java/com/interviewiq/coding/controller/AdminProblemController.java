package com.interviewiq.coding.controller;

import com.interviewiq.coding.dto.CodingDtos.*;
import com.interviewiq.coding.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Path mein "/admin/" hai, isliye gateway sirf ADMIN role walon ko aane deta hai.
 * Admin banane ke liye: UPDATE auth_db.users SET role='ADMIN' WHERE email='...'; phir dobara login karo.
 */
@RestController
@RequestMapping("/coding/admin/problems")
public class AdminProblemController {

    private final ProblemService problemService;

    public AdminProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public List<AdminProblemSummary> list() {
        return problemService.adminList();
    }

    @GetMapping("/{id}")
    public AdminProblemResponse get(@PathVariable Long id) {
        return problemService.adminGet(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProblemResponse create(@Valid @RequestBody ProblemUpsertRequest request) {
        return problemService.create(request);
    }

    /** Sheet/JSON se bulk upload. Format: docs/problems/problems-template.json */
    @PostMapping("/import")
    public ImportResponse importProblems(@Valid @RequestBody ImportRequest request) {
        return problemService.importAll(request);
    }

    @PutMapping("/{id}")
    public AdminProblemResponse update(@PathVariable Long id, @Valid @RequestBody ProblemUpsertRequest request) {
        return problemService.update(id, request);
    }
}
