package com.interviewiq.coding.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Expected aur actual output compare karta hai. Trailing spaces / trailing blank lines / \r\n ignore.
 * Beech ka whitespace aur case SAME hona chahiye.
 */
public final class OutputComparator {

    private OutputComparator() {}

    public static boolean matches(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    private static List<String> normalize(String text) {
        List<String> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }
        for (String line : text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            lines.add(line.stripTrailing());
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }
}
