package com.nitin.AptituteQuestionsService.model;

public enum QuestionCategory {
    QUANTITATIVE("Quantitative Aptitude"),
    LOGICAL_REASONING("Logical Reasoning"),
    VERBAL_ABILITY("Verbal Ability"),
    DATA_INTERPRETATION("Data Interpretation");

    private final String displayName;

    QuestionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static QuestionCategory fromString(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("ALL")) {
            return null;
        }
        for (QuestionCategory category : QuestionCategory.values()) {
            if (category.name().equalsIgnoreCase(value.trim()) ||
                category.displayName.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return null;
    }
}
