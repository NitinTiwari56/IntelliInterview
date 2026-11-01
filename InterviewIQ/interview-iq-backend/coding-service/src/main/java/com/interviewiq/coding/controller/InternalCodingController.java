package com.interviewiq.coding.controller;

import com.interviewiq.coding.dto.CodingDtos.CodingSummary;
import com.interviewiq.coding.service.CodingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sirf backend services ke liye (analytics-service). Gateway pe /internal/** ka route nahi hai. */
@RestController
@RequestMapping("/internal/coding")
public class InternalCodingController {

    private final CodingService codingService;

    public InternalCodingController(CodingService codingService) {
        this.codingService = codingService;
    }

    @GetMapping("/{studentId}/summary")
    public CodingSummary summary(@PathVariable Long studentId) {
        return codingService.summary(studentId);
    }
}
