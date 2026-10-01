package com.nitin.AptituteQuestionsService.model;

public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    public static DifficultyLevel fromString(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("MIXED") || value.equalsIgnoreCase("ALL")) {
            return null;
        }
        for (DifficultyLevel level : DifficultyLevel.values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        return null;
    }
}
