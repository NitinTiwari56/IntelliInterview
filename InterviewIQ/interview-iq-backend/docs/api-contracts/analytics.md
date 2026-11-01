# 6. Analytics APIs: Dashboard + Roadmap (gateway: http://localhost:8080)

Sab endpoints mein header: `Authorization: Bearer <token>`
STATUS: backend testing chal rahi hai, field names thode badal sakte hain.

## Ye kya hai
Resume, Interview, Coding aur Aptitude ke scores ek jagah jodke **dashboard** banta hai, aur weak areas ke hisaab se
**personalized roadmap** (kya practice karni hai, kitne ghante, kis priority se).
- Ek module ka data na ho (student ne abhi kiya hi nahi) toh baaki se dashboard phir bhi banta hai.
- Kuch bhi nahi kiya ho toh **generic starter roadmap** milta hai (`genericStarter: true`).
- **Dashboard pehle, roadmap baad mein** call karo (ek saath parallel nahi). Pehli baar dono ek hi student ka profile banate hain.

## GET /analytics/dashboard
```json
{
  "studentId": 7, "targetRole": "Backend Engineer", "hasData": true,
  "overallReadinessScore": 68.4, "readinessLevel": "Needs Targeted Improvement",
  "moduleScores": {
    "resumeAtsScore": 82.0, "technicalInterviewScore": 70.0, "hrInterviewScore": null,
    "codingScore": 41.5, "codingProblemsSolved": 3, "aptitudeScore": 70.0,
    "aptitudeBreakdown": { "quant": 65.0, "logical": 75.0, "verbal": 70.0 }
  },
  "categoryChartData": [ { "category": "Resume ATS", "score": 82.0, "normalizedScore": 82.0, "color": "#4F46E5" } ],
  "weakAreas": [ { "category": "CODING", "severity": "HIGH", "score": 41.5, "gapDescription": "..." } ],
  "strengths": ["Strong resume profile matching target industry roles."],
  "serviceStatus": { "ResumeService": "AVAILABLE", "CodingService": "UNAVAILABLE_OR_EMPTY" }
}
```
- Scores 0-100. `null` = us module ka data nahi (chart mein mat dikhao ya "Not attempted" likho).
- `readinessLevel`: `Placement Ready` (80+) | `Needs Targeted Improvement` (65-79) | `Getting Started` (<65)
- `severity`: `HIGH` (<60) | `MEDIUM` (<threshold). Weak area categories: RESUME, TECHNICAL_INTERVIEW, HR_INTERVIEW, CODING, APTITUDE
- `categoryChartData` seedha bar/radar chart ko de sakte ho (`color` diya hua hai).
- Overall readiness weights: resume 20%, technical interview 30%, HR 15%, coding 20%, aptitude 15% (jo modules ka data hai unhi mein baanta jata hai).
- **Coding score** = 70% (difficulty-weighted solved problems) + 30% acceptance rate.

## GET /analytics/roadmap
```json
{
  "studentId": 7, "targetRole": "Backend Engineer", "genericStarter": false,
  "overallReadinessScore": 68.4, "summaryVerdict": "...", "totalEstimatedHours": 24,
  "recommendedActions": [
    { "stepNumber": 1, "title": "Practice medium DSA problems", "category": "CODING", "priority": "HIGH",
      "actionType": "PRACTICE_CODING", "description": "...", "estimatedHours": 8,
      "suggestedTarget": "Solve 5 medium problems", "status": "PENDING" }
  ],
  "identifiedWeakAreas": [ { "category": "CODING", "severity": "HIGH", "score": 41.5, "gapDescription": "..." } ],
  "generatedAt": "2026-10-10T10:00:00Z"
}
```
- `actionType`: PRACTICE_CODING | RETAKE_MOCK_INTERVIEW | UPDATE_RESUME | PRACTICE_APTITUDE | REVISE_CONCEPTS
  (UI mein har type ko apne page ka button/link banao: Coding page, Interview page, Resume page, Aptitude page)
- Cached roadmap **30 min** baad apne aap naya banta hai.

## POST /analytics/roadmap/regenerate   (body nahi)
"Refresh" button: abhi ke latest scores se roadmap turant dobara. AI chale toh 3-10 sec lag sakte hain, loading dikhao.

## GET /analytics/roadmap/health
`{ "status": "UP", "service": "analytics-service", "aiConfigured": false, "engineMode": "RULE_BASED_ROADMAP" }`

## Resume wale roadmap se farak
`GET /resume/roadmap` sirf **skills ka checklist** hai (resume ke hisaab se). `GET /analytics/roadmap` **poora preparation plan** hai
(coding + aptitude + interview + resume sab milake).

## Errors
`{ "timestamp": "...", "status": 500, "error": "...", "message": "..." }` : `message` dikhao. `401` token nahi.
