package com.interviewiq.resume.service;

import com.interviewiq.resume.dto.extracted.*;
import com.interviewiq.resume.model.RoleSkillProfile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Resume ki STRUCTURED details (skills, projects, experience, certifications) ko target role ke
 * skills se match karta hai. Bina AI, bina PDF: edit ke turant baad chalta hai.
 * rawText use nahi hota, warna student skill hata bhi de toh wo match hoti rehti.
 * Score = skill match score (0-100): core 60% + secondary 30% + tools 10%. Ye ATS score nahi hai.
 */
@Component
public class SkillGapCalculator {

    public record Item(String skill, String priority, boolean done) {}

    public record Result(RoleSkillProfile profile, List<Item> items, int score, int covered, int total,
                         List<String> matched, List<String> missing, List<String> userSkills) {}

    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
            Map.entry("sql", List.of("mysql", "postgres", "postgresql", "sqlite", "oracle db")),
            Map.entry("oop", List.of("object oriented", "object-oriented", "oops")),
            Map.entry("datastructures", List.of("data structure", "dsa")),
            Map.entry("algorithms", List.of("algorithm", "dsa")),
            Map.entry("restapis", List.of("rest api", "restful")),
            Map.entry("springboot", List.of("spring")),
            Map.entry("react", List.of("react")),
            Map.entry("node", List.of("node.js", "nodejs")),
            Map.entry("dbms", List.of("database management", "databases")),
            Map.entry("os", List.of("operating system")),
            Map.entry("operatingsystems", List.of("operating system"))
    );

    private final TargetRoleService targetRoleService;

    public SkillGapCalculator(TargetRoleService targetRoleService) {
        this.targetRoleService = targetRoleService;
    }

    public Result compute(ExtractedResumeData data, String roleName) {
        RoleSkillProfile profile = targetRoleService.getRoleProfile(roleName);
        Haystack hay = Haystack.from(data);

        List<Item> items = new ArrayList<>();
        addItems(items, profile.getCoreSkills(), "CORE", hay);
        addItems(items, profile.getSecondarySkills(), "SECONDARY", hay);
        addItems(items, profile.getToolsAndFrameworks(), "TOOL", hay);

        double core = ratio(items, "CORE");
        double secondary = ratio(items, "SECONDARY");
        double tools = ratio(items, "TOOL");
        int score = (int) Math.round(core * 60 + secondary * 30 + tools * 10);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Item item : items) {
            (item.done() ? matched : missing).add(item.skill());
        }
        int covered = matched.size();
        return new Result(profile, items, score, covered, items.size(), matched, missing, hay.skillNames);
    }

    // ---------------------------------------------------------------------

    private void addItems(List<Item> items, List<String> skills, String priority, Haystack hay) {
        if (skills == null) {
            return;
        }
        for (String skill : skills) {
            boolean done = isPresent(skill, hay);
            // same skill do categories mein ho (jaise Git) toh ek hi baar
            if (items.stream().noneMatch(i -> i.skill().equalsIgnoreCase(skill))) {
                items.add(new Item(skill, priority, done));
            }
        }
    }

    private double ratio(List<Item> items, String priority) {
        long total = items.stream().filter(i -> i.priority().equals(priority)).count();
        if (total == 0) {
            return 1.0;
        }
        long done = items.stream().filter(i -> i.priority().equals(priority) && i.done()).count();
        return (double) done / total;
    }

    private boolean isPresent(String roleSkill, Haystack hay) {
        for (String alt : alternatives(roleSkill)) {
            String key = key(alt);
            if (!key.isEmpty() && hay.listKeys.contains(key)) {
                return true;
            }
            if (alt.length() >= 2 && containsWord(hay.text, alt)) {
                return true;
            }
            for (String extra : ALIASES.getOrDefault(key, List.of())) {
                if (containsWord(hay.text, extra)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** "Authentication (JWT/OAuth)" -> authentication, jwt, oauth. "HTML5" -> html5, html. */
    static Set<String> alternatives(String skill) {
        String lower = skill.toLowerCase(Locale.ROOT).trim();
        Set<String> alts = new LinkedHashSet<>();
        alts.add(lower);

        String outside = lower.replaceAll("\\([^)]*\\)", " ").trim();
        if (!outside.isBlank()) {
            alts.add(outside);
        }
        java.util.regex.Matcher m = Pattern.compile("\\(([^)]*)\\)").matcher(lower);
        List<String> pieces = new ArrayList<>(alts);
        while (m.find()) {
            pieces.add(m.group(1));
        }
        for (String piece : pieces) {
            for (String part : piece.split("[/,]")) {
                String p = part.trim();
                if (!p.isEmpty()) {
                    alts.add(p);
                }
            }
        }
        for (String a : new ArrayList<>(alts)) {
            if (a.matches("^[a-z]{3,}\\d+$")) {
                alts.add(a.replaceAll("\\d+$", ""));
            }
        }
        return alts;
    }

    /** Letters/digits/+/# bacha ke chhota karo; "nodejs"->"node", "reactjs"->"react". */
    static String key(String s) {
        String k = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#]", "");
        if (k.length() > 4 && k.endsWith("js")) {
            k = k.substring(0, k.length() - 2);
        }
        return k;
    }

    private static boolean containsWord(String text, String phrase) {
        return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(phrase.toLowerCase(Locale.ROOT)) + "(?![a-z0-9])")
                .matcher(text).find();
    }

    // ---------------------------------------------------------------------

    private static final class Haystack {
        final Set<String> listKeys = new HashSet<>();
        final List<String> skillNames = new ArrayList<>();
        String text = "";

        static Haystack from(ExtractedResumeData data) {
            Haystack h = new Haystack();
            if (data == null) {
                return h;
            }
            StringBuilder sb = new StringBuilder();

            CategorizedSkills s = data.getSkills();
            if (s != null) {
                for (List<String> list : List.of(s.getLanguages(), s.getFrameworks(),
                        s.getToolsAndDatabases(), s.getCoreSubjects())) {
                    for (String item : list) {
                        h.addSkill(item, sb);
                    }
                }
            }
            for (ProjectItem p : data.getProjects()) {
                for (String tech : p.getTechStack()) {
                    h.addSkill(tech, sb);
                }
                append(sb, p.getTitle());
                append(sb, p.getDescription());
            }
            for (ExperienceItem e : data.getExperience()) {
                append(sb, e.getCompany());
                append(sb, e.getRole());
                for (String hl : e.getHighlights()) {
                    append(sb, hl);
                }
            }
            for (String cert : data.getCertifications()) {
                append(sb, cert);
            }
            h.text = sb.toString().toLowerCase(Locale.ROOT);
            return h;
        }

        private void addSkill(String item, StringBuilder sb) {
            if (item == null || item.isBlank()) {
                return;
            }
            String k = key(item);
            if (!k.isEmpty()) {
                listKeys.add(k);
            }
            if (skillNames.stream().noneMatch(x -> x.equalsIgnoreCase(item.trim()))) {
                skillNames.add(item.trim());
            }
            append(sb, item);
        }

        private static void append(StringBuilder sb, String s) {
            if (s != null && !s.isBlank()) {
                sb.append(s).append(" | ");
            }
        }
    }
}
