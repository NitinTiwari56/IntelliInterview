package com.interviewiq.resume.service;

import com.interviewiq.resume.dto.extracted.CategorizedSkills;
import com.interviewiq.resume.dto.extracted.ExtractedResumeData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SkillGapCalculatorTest {

    private final SkillGapCalculator calculator = new SkillGapCalculator(new TargetRoleService());

    private ExtractedResumeData dataWith(List<String> languages, List<String> subjects) {
        ExtractedResumeData data = new ExtractedResumeData();
        CategorizedSkills skills = new CategorizedSkills();
        skills.setLanguages(languages);
        skills.setCoreSubjects(subjects);
        data.setSkills(skills);
        return data;
    }

    @Test
    @DisplayName("Skills in the structured lists are matched; missing ones are reported")
    void matchesListedSkills() {
        var result = calculator.compute(dataWith(List.of("Java", "SQL"), List.of("Data Structures")),
                "Software Development Engineer (SDE)");

        assertThat(result.matched()).contains("Java", "Data Structures", "SQL");
        assertThat(result.missing()).contains("C++");
        assertThat(result.score()).isBetween(1, 99);
    }

    @Test
    @DisplayName("Adding a missing skill raises the score and removing it lowers it again")
    void editingChangesScore() {
        var before = calculator.compute(dataWith(List.of("Java"), List.of()), "Software Development Engineer (SDE)");
        var after = calculator.compute(dataWith(List.of("Java", "C++"), List.of()), "Software Development Engineer (SDE)");
        var removed = calculator.compute(dataWith(List.of("Java"), List.of()), "Software Development Engineer (SDE)");

        assertThat(after.score()).isGreaterThan(before.score());
        assertThat(after.matched()).contains("C++");
        assertThat(removed.score()).isEqualTo(before.score());
    }

    @Test
    @DisplayName("Empty resume data gives score 0 and does not crash")
    void emptyData() {
        var result = calculator.compute(new ExtractedResumeData(), "Backend Engineer");
        assertThat(result.score()).isZero();
        assertThat(result.matched()).isEmpty();
    }

    @Test
    @DisplayName("Compound role skills like 'Authentication (JWT/OAuth)' match any alternative")
    void compoundSkills() {
        var result = calculator.compute(dataWith(List.of(), List.of("JWT")), "Backend Engineer");
        assertThat(result.matched()).contains("Authentication (JWT/OAuth)");
    }
}
