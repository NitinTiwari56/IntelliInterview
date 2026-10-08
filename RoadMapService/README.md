# RoadMapService (Analytics & Personalized Roadmap Engine)

`RoadMapService` is the central intelligence hub of **Interview IQ**. It aggregates cross-service performance data from Resume Analysis, Technical & HR Interviews, DSA Coding Practice, and Timed Aptitude tests. Using this data, it detects candidate weaknesses, computes placement readiness indicators, generates chart distributions, and produces prioritized, actionable preparation roadmaps.

---

## 📋 SRS Requirements & Feature Matrix

| SRS Requirement | Description | Implementation in RoadMapService |
| :--- | :--- | :--- |
| **REQ-DSH-1** | Collect data from Resume, Interview, Coding, and Aptitude services | Implemented via [CrossServiceClient](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/client/CrossServiceClient.java) per SRS Figure B-6. |
| **REQ-DSH-2** | Show ATS score, interview scores, coding progress & aptitude scores | Consolidated in [DashboardResponse](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/dto/DashboardResponse.java). |
| **REQ-DSH-3** | Overall readiness indicator (TBD-8: weighted average of module scores) | Computed in [AnalyticsServiceImpl](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/service/AnalyticsServiceImpl.java). |
| **REQ-DSH-4** | Present performance by category or module using charts | Slices generated via [ChartSliceDto](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/dto/ChartSliceDto.java). |
| **REQ-DSH-5** | Viewing dashboard does not change stored student data | Read-only aggregation query endpoints. |
| **REQ-DSH-6** | Empty-state message when candidate has no data | Returns `hasData: false` and starter guidance. |
| **REQ-DSH-7** | Resilience: failure of one service shall not block other modules | Fault-tolerant timeouts with partial degradation support. |
| **REQ-RMP-1,2** | Detect weak areas by comparing normalized category scores (TBD-9) | Evaluated using thresholds (<65% HIGH, <75% MEDIUM). |
| **REQ-RMP-3** | Weak areas stored in `performance_profile` (1 profile per student) | Persisted via [PerformanceProfileEntity](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/model/PerformanceProfileEntity.java). |
| **REQ-RMP-4** | Generate ordered, prioritized list of actionable recommendations | [AiRoadmapEngineService](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/service/AiRoadmapEngineService.java) and [RuleBasedRoadmapEngineService](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/service/RuleBasedRoadmapEngineService.java). |
| **REQ-RMP-5** | Roadmap (1 per student) stored with generation timestamp | Persisted via [RoadmapEntity](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/model/RoadmapEntity.java). |
| **REQ-RMP-6** | Consume asynchronous broker events to update signals | [AnalyticsEventListener](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/consumer/AnalyticsEventListener.java) for Kafka topics (Table 3-4). |
| **REQ-RMP-7** | If not enough data, show generic starter roadmap | Default 5-step foundational roadmap. |
| **REQ-RMP-8** | Publish `RoadmapUpdated` event when roadmap changes | Dispatched via [RoadmapEventPublisher](file:///c:/Users/ACER/Downloads/InteliInterview/RoadMapService/src/main/java/com/RoadMapService/RoadMapService/service/RoadmapEventPublisher.java) for Notification Service. |

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/dashboard/{studentId}` | Aggregated performance dashboard with module scores, readiness, and chart data |
| `GET` | `/api/roadmap/{studentId}` | Fetch the candidate's personalized roadmap (or starter roadmap) |
| `POST` | `/api/roadmap/{studentId}/regenerate` | Force cross-service re-aggregation and generate updated roadmap |
| `GET` | `/api/roadmap/health` | Health & AI engine status |

---

## 📥 Complete Inputs and Outputs

### 1. Performance Dashboard (`GET /api/dashboard/{studentId}`)

#### Case A: Candidate With Completed Activities Across Modules
- **Input:**
  - **Method:** `GET`
  - **URL:** `http://localhost:8086/api/dashboard/101`
  - **Headers / Body:** *(None)*

- **Output (`200 OK`):**
```json
{
  "studentId": 101,
  "targetRole": "Java Backend Developer",
  "hasData": true,
  "overallReadinessScore": 76.5,
  "readinessLevel": "Needs Targeted Improvement",
  "moduleScores": {
    "resumeAtsScore": 84.0,
    "technicalInterviewScore": 62.0,
    "hrInterviewScore": 80.0,
    "codingScore": 75.0,
    "codingProblemsSolved": 8,
    "aptitudeScore": 70.0,
    "aptitudeBreakdown": {
      "quant": 68.0,
      "logical": 74.0,
      "verbal": 70.0
    }
  },
  "categoryChartData": [
    {
      "category": "Resume ATS",
      "score": 84.0,
      "normalizedScore": 84.0,
      "color": "#4F46E5"
    },
    {
      "category": "Technical Interview",
      "score": 62.0,
      "normalizedScore": 62.0,
      "color": "#06B6D4"
    },
    {
      "category": "HR Interview",
      "score": 80.0,
      "normalizedScore": 80.0,
      "color": "#10B981"
    },
    {
      "category": "DSA Coding",
      "score": 75.0,
      "normalizedScore": 75.0,
      "color": "#F59E0B"
    },
    {
      "category": "Aptitude",
      "score": 70.0,
      "normalizedScore": 70.0,
      "color": "#EC4899"
    }
  ],
  "weakAreas": [
    {
      "category": "TECHNICAL_INTERVIEW",
      "severity": "HIGH",
      "score": 62.0,
      "gapDescription": "Technical interview fundamentals require deeper explanation of internal mechanisms and concurrency."
    },
    {
      "category": "APTITUDE",
      "severity": "MEDIUM",
      "score": 70.0,
      "gapDescription": "Timed aptitude test score is below target threshold; speed and calculation accuracy need improvement."
    }
  ],
  "strengths": [
    "Strong resume profile matching target industry roles.",
    "Confident communication and structured responses in HR mock rounds.",
    "Solid algorithmic problem solving and clean implementation."
  ],
  "serviceStatus": {
    "ResumeService": "AVAILABLE",
    "InterviewService": "AVAILABLE",
    "CodingService": "AVAILABLE",
    "AptitudeService": "AVAILABLE"
  }
}
```

#### Case B: Brand New Candidate (Empty State - REQ-DSH-6)
- **Input:**
  - **Method:** `GET`
  - **URL:** `http://localhost:8086/api/dashboard/999`
  - **Headers / Body:** *(None)*

- **Output (`200 OK`):**
```json
{
  "studentId": 999,
  "targetRole": "Software Development Engineer",
  "hasData": false,
  "overallReadinessScore": 50.0,
  "readinessLevel": "Getting Started",
  "moduleScores": {
    "resumeAtsScore": null,
    "technicalInterviewScore": null,
    "hrInterviewScore": null,
    "codingScore": null,
    "codingProblemsSolved": 0,
    "aptitudeScore": null
  },
  "categoryChartData": [],
  "weakAreas": [],
  "strengths": [],
  "serviceStatus": {
    "ResumeService": "UNAVAILABLE_OR_EMPTY",
    "InterviewService": "UNAVAILABLE_OR_EMPTY",
    "CodingService": "UNAVAILABLE_OR_EMPTY",
    "AptitudeService": "UNAVAILABLE_OR_EMPTY"
  }
}
```

---

### 2. Personalized Roadmap (`GET /api/roadmap/{studentId}`)

#### Case A: Customized Weakness-Driven Preparation Plan
- **Input:**
  - **Method:** `GET`
  - **URL:** `http://localhost:8086/api/roadmap/101`
  - **Headers / Body:** *(None)*

- **Output (`200 OK`):**
```json
{
  "studentId": 101,
  "targetRole": "Java Backend Developer",
  "isGenericStarter": false,
  "overallReadinessScore": 76.5,
  "summaryVerdict": "Solid foundation established. Focusing effort on the identified high-priority weakness categories (Technical Interview Concurrency & Quantitative Aptitude) will yield the fastest placement readiness gains.",
  "totalEstimatedHours": 7,
  "recommendedActions": [
    {
      "stepNumber": 1,
      "title": "Retake Technical Mock Interview (Focus on Architectural Trade-offs)",
      "category": "TECHNICAL_INTERVIEW",
      "priority": "HIGH",
      "actionType": "RETAKE_MOCK_INTERVIEW",
      "description": "Review question feedback and re-attempt technical mock interview focusing on concurrency, thread-safety, and internal mechanisms.",
      "estimatedHours": 3,
      "suggestedTarget": "Score > 75%",
      "status": "PENDING"
    },
    {
      "stepNumber": 2,
      "title": "Daily 15-Minute Aptitude Drills",
      "category": "APTITUDE",
      "priority": "MEDIUM",
      "actionType": "PRACTICE_APTITUDE",
      "description": "Practice speed calculation for Percentages, Profit/Loss, and Syllogisms within 30-second timers.",
      "estimatedHours": 2,
      "suggestedTarget": "Accuracy > 75%",
      "status": "PENDING"
    },
    {
      "stepNumber": 3,
      "title": "Optimize Resume Keywords for Java Backend Developer",
      "category": "RESUME",
      "priority": "HIGH",
      "actionType": "UPDATE_RESUME",
      "description": "Incorporate critical missing keywords into your projects: Kafka, Docker, System Design",
      "estimatedHours": 2,
      "suggestedTarget": "Achieve ATS Score > 80%",
      "status": "PENDING"
    }
  ],
  "identifiedWeakAreas": [
    {
      "category": "TECHNICAL_INTERVIEW",
      "severity": "HIGH",
      "score": 62.0,
      "gapDescription": "Technical interview fundamentals require deeper explanation of internal mechanisms and concurrency."
    },
    {
      "category": "APTITUDE",
      "severity": "MEDIUM",
      "score": 70.0,
      "gapDescription": "Timed aptitude test score is below target threshold; speed and calculation accuracy need improvement."
    }
  ],
  "generatedAt": "2026-10-08T14:10:00Z"
}
```

#### Case B: Generic Starter Roadmap (When Candidate Has No Data - REQ-RMP-7)
- **Input:**
  - **Method:** `GET`
  - **URL:** `http://localhost:8086/api/roadmap/999`
  - **Headers / Body:** *(None)*

- **Output (`200 OK`):**
```json
{
  "studentId": 999,
  "targetRole": "Software Development Engineer",
  "isGenericStarter": true,
  "overallReadinessScore": 50.0,
  "summaryVerdict": "Welcome to Interview IQ! Complete your baseline assessments to generate your customized AI weakness roadmap.",
  "totalEstimatedHours": 8,
  "recommendedActions": [
    {
      "stepNumber": 1,
      "title": "Upload Resume & Target Role for ATS Gap Analysis",
      "category": "RESUME",
      "priority": "HIGH",
      "actionType": "UPDATE_RESUME",
      "description": "Upload your latest PDF resume targeting Software Development Engineer to receive immediate ATS keyword gap scoring.",
      "estimatedHours": 1,
      "suggestedTarget": "Target ATS Score: 80%+",
      "status": "PENDING"
    },
    {
      "stepNumber": 2,
      "title": "Attempt 1 Technical Mock Interview",
      "category": "TECHNICAL_INTERVIEW",
      "priority": "HIGH",
      "actionType": "RETAKE_MOCK_INTERVIEW",
      "description": "Complete a 5-question AI Technical Interview to evaluate core language fundamentals and architecture skills.",
      "estimatedHours": 2,
      "suggestedTarget": "Target Score: 75%+",
      "status": "PENDING"
    },
    {
      "stepNumber": 3,
      "title": "Solve 3 Core DSA Coding Problems",
      "category": "CODING",
      "priority": "HIGH",
      "actionType": "PRACTICE_CODING",
      "description": "Practice Arrays, HashMaps, and Two-Pointer problems in the coding sandbox.",
      "estimatedHours": 3,
      "suggestedTarget": "Pass all test cases",
      "status": "PENDING"
    },
    {
      "stepNumber": 4,
      "title": "Take a 10-Question Timed Aptitude Drill",
      "category": "APTITUDE",
      "priority": "MEDIUM",
      "actionType": "PRACTICE_APTITUDE",
      "description": "Practice speed and accuracy across Quantitative and Logical aptitude questions with a 30-second timer.",
      "estimatedHours": 1,
      "suggestedTarget": "Score 70%+",
      "status": "PENDING"
    },
    {
      "stepNumber": 5,
      "title": "Take 1 HR Mock Behavioral Interview",
      "category": "HR_INTERVIEW",
      "priority": "MEDIUM",
      "actionType": "RETAKE_MOCK_INTERVIEW",
      "description": "Practice answering common situational questions using the STAR framework.",
      "estimatedHours": 1,
      "suggestedTarget": "Score 80%+",
      "status": "PENDING"
    }
  ],
  "identifiedWeakAreas": [
    {
      "category": "BASELINE_ASSESSMENT",
      "severity": "MEDIUM",
      "score": 50.0,
      "gapDescription": "Complete module activities to establish accurate baseline data."
    }
  ],
  "generatedAt": "2026-10-08T14:10:00Z"
}
```

---

### 3. Force Regenerate Roadmap (`POST /api/roadmap/{studentId}/regenerate`)

- **Input:**
  - **Method:** `POST`
  - **URL:** `http://localhost:8086/api/roadmap/101/regenerate`
  - **Headers / Body:** *(None)*
- **Output (`200 OK`):** Updated `RoadmapResponse` object with recalculated weak areas and recommendations.

---

### 4. Health Check (`GET /api/roadmap/health`)

- **Input:**
  - **Method:** `GET`
  - **URL:** `http://localhost:8086/api/roadmap/health`
- **Output (`200 OK`):**
```json
{
  "service": "RoadMapService",
  "status": "UP",
  "aiConfigured": false,
  "engineMode": "INTELLIGENT_RULE_BASED_ROADMAP"
}
```

---

## 🗄️ Database Schemas (SRS Table 6-1)

### Table 1: `performance_profile`
- `profile_id` (PK, Long)
- `student_id` (Unique, Long)
- `overall_readiness_score` (Double)
- `resume_score` (Double)
- `technical_interview_score` (Double)
- `hr_interview_score` (Double)
- `coding_score` (Double)
- `aptitude_score` (Double)
- `weak_areas` (JSON array of `WeakAreaItem`)
- `strengths` (JSON array)
- `last_updated` (Timestamp UTC)

### Table 2: `roadmap`
- `roadmap_id` (PK, Long)
- `student_id` (Unique, Long)
- `target_role` (Varchar)
- `is_generic_starter` (Boolean)
- `recommended_actions` (JSON array of `RoadmapActionItem`)
- `summary_verdict` (Text)
- `generated_at` (Timestamp UTC)

---

## 🚀 Running the Service

### Run with Maven:
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-22"
cd RoadMapService
.\mvnw.cmd spring-boot:run
```
Default Port: `8086`
H2 Console: `http://localhost:8086/h2-console` (JDBC URL: `jdbc:h2:mem:analyticsdb`, user: `sa`)
