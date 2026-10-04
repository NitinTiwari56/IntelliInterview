# Resume Service (`ResumeService`)

An enterprise-grade, AI-driven microservice engineered for the **Interview IQ** placement preparation platform. The service processes candidate resumes (PDF format up to 2 MB), extracts comprehensive structured candidate profiles, executes in-depth ATS scoring and skill gap analysis against industry target roles, stores attempts persistently, and streams events to downstream microservices.

Powered by a **Dual-Engine Architecture**: **Groq Cloud AI** (`openai/gpt-oss-120b` via OpenAI-compatible Spring AI) with an automatic zero-latency **Rule-Based ATS Keyword & Heuristic Fallback Engine**.

---

## Table of Contents
1. [Overview & Core Value](#1-overview--core-value)
2. [SRS Compliance Matrix](#2-srs-compliance-matrix)
3. [Architecture & Workflow](#3-architecture--workflow)
4. [Dual-Engine Analysis Strategy](#4-dual-engine-analysis-strategy)
5. [Structured Extraction System](#5-structured-extraction-system)
6. [Prerequisites & Configuration](#6-prerequisites--configuration)
7. [API Reference & Usage](#7-api-reference--usage)
8. [Complete Response Schema & Contract](#8-complete-response-schema--contract)
9. [Database Architecture](#9-database-architecture)
10. [Inter-Service Integration](#10-inter-service-integration)
11. [Running & Verification](#11-running--verification)
12. [Troubleshooting & FAQ](#12-troubleshooting--faq)

---

## 1. Overview & Core Value

In campus placement and engineering hiring cycles, resumes are the primary filter. Students frequently face automated rejections from Applicant Tracking Systems (ATS) due to keyword mismatches, poor formatting, or absence of quantifiable impact metrics.

`ResumeService` delivers:
- **Direct PDF Ingestion**: Accepts raw `.pdf` files up to 2 MB via `multipart/form-data`.
- **Deep Text & Metadata Extraction**: Uses **Apache PDFBox 3.x** to parse and structure candidate identity, education history, internships/experience, projects, categorized skills, certifications, and complete raw text.
- **Role-Aligned ATS Scoring**: Evaluates candidate readiness against target engineering job roles (SDE, Backend, Frontend, Full Stack, DevOps, Data Science/ML, Mobile Developer, QA).
- **Skill Gap Detection**: Computes matched skills versus missing skills required for the chosen role.
- **Actionable Optimization Feedback**: Suggests concrete improvements (missing keywords, X-Y-Z quantifiable metric formatting, ATS layout adjustments).
- **Permanent Audit Trail**: Stores all parsed components and historical attempts in the database.
- **Cross-Service Ecosystem Enablement**: Publishes `ResumeAnalyzed` events for the **Analytics Service** and serves candidate profile data to the **AI Interview Service**.

---

## 2. SRS Compliance Matrix

The service fully satisfies all functional and non-functional requirements specified in the Interview IQ SRS v1.0:

| Requirement ID | Specification | Status | Implementation Details |
| :--- | :--- | :---: | :--- |
| **REQ-RES-1** | Student uploads resume file (PDF) and chooses target job role |  | Handled via `POST /api/v1/resumes/analyze` accepting `file` and `targetRole`. |
| **REQ-RES-2** | Check file type & size: accept only PDF up to 2 MB; reject others with explicit error message |  | `PdfParserService` strictly checks MIME type, extension, and 2 MB boundary. Throws `UnsupportedFileException` or `FileSizeLimitExceededException`. |
| **REQ-RES-3** | Parse PDF and extract raw text |  | `PdfParserService` powered by Apache PDFBox 3.x `Loader.loadPDF()`. |
| **REQ-RES-4** | Send resume text and target role to LLM using fixed prompt template |  | `AiResumeAnalysisService` with structured system prompt template. |
| **REQ-RES-5** | Extract skills and compare against skills required for target role |  | `TargetRoleService` provides predefined skill profiles; dual-engine compares matched and missing skills. |
| **REQ-RES-6** | Return ATS-style score, extracted skills, missing skills, and suggestions |  | Returned in `ResumeAnalysisResponse` JSON payload. |
| **REQ-RES-7** | Store every analysis as a new row in `resumes` table without overwriting |  | `ResumeEntity` and `ResumeRepository` save every attempt with UTC timestamp. |
| **REQ-RES-8** | Publish `ResumeAnalyzed` event for Analytics Service |  | `ResumeEventPublisher` publishes `ResumeAnalyzedEvent` (Table 3-4). |
| **REQ-RES-9** | Analysis returned within 5 seconds under normal load (PERF-1) |  | Rule-based engine runs in **<50ms**; Groq LPU AI inference runs in **1–2 seconds**. |
| **REQ-RES-10** | Student can view history of previous analyses and scores |  | Exposed via `GET /api/v1/resumes/history/{studentId}`. |
| **REQ-RES-11** | Failure/timeout handling: report error, save no incomplete record, allow retry |  | `@Transactional` rollback ensures no partial or corrupted records are saved on failure. |
| **REQ-RES-12** | Fixed JSON response contract agreed before frontend integration (TBD-3) |  | Formalized in `ResumeAnalysisResponse` including both ATS analysis and `extractedData`. |
| **DB-1 - DB-4** | MySQL 8 ready, isolated schema, UTC timestamps, `resumes` table structure |  | `resumes` table with JPA converter for JSON columns, H2 in-memory default, MySQL 8 profile. |

---

## 3. Architecture & Workflow

```
[ React.js Web Client / API Gateway ]
               │
               ▼  POST /api/v1/resumes/analyze (multipart/form-data: PDF + targetRole)
 ┌─────────────────────────────────────────────────────────────┐
 │                      ResumeController                       │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                      ResumeServiceImpl                      │
 ├──────────────────────────────┬──────────────────────────────┤
 │ 1. Validate File & Size      │  PdfParserService (PDFBox)   │
 │ 2. Extract Full Raw Text     │                              │
 │ 3. Persist PDF to Disk       │  FileStorageService          │
 └──────────────────────────────┼──────────────────────────────┘
                                │
                                ▼
                   ┌───────────────────────────┐
                   │  Groq API Key Configured? │
                   └─────────────┬─────────────┘
                        YES      │             NO / Timeout / Rate Limit
              ┌──────────────────┴──────────────────┐
              ▼                                     ▼
 ┌───────────────────────────┐        ┌───────────────────────────┐
 │       Groq AI Engine      │        │  Rule-Based ATS Analyzer  │
 │  (Spring AI / ChatModel)  │        │   (Zero-Downtime Engine)  │
 │                           │        │                           │
 │ • Deep contextual analysis│        │ • Skill & Keyword Matrix  │
 │ • Actionable suggestions  │        │ • ATS Formatting Scorer   │
 │ • Recruiter verdict       │        │ • Action Verb/Metric Scan │
 │ • Profile structuring     │        │ • Regex Entity Extractor  │
 └─────────────┬─────────────┘        └─────────────┬─────────────┘
               │                                    │
               └─────────────────┬──────────────────┘
                                 │
                                 ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ 4. Persist to Database (`resumes` table via JPA)            │
 │ 5. Dispatch `ResumeAnalyzed` Event -> Analytics Service     │
 │ 6. Return Unified JSON Response                             │
 └─────────────────────────────────────────────────────────────┘
```

---

## 4. Dual-Engine Analysis Strategy

To guarantee **100% service uptime** regardless of external network stability, API quotas, or third-party latency, the service uses a dual-engine pattern:

### 1. Primary: Groq AI Engine (`AiResumeAnalysisService`)
* Uses Groq Cloud's ultra-fast LPU inference (`openai/gpt-oss-120b`).
* Evaluates semantic alignment, nuanced phrasing, depth of experience, and contextual project impact.
* Average latency: **1.0 – 2.0 seconds**.

### 2. Fallback: Rule-Based ATS & Heuristic Analyzer (`RuleBasedAtsAnalyzerService`)
* Activates automatically if `GROQ_API_KEY` is omitted, or if the external AI service times out or hits rate limits.
* Evaluates keyword density against role-specific skill matrices, computes ATS formatting scores across standard resume sections, checks for action verbs and quantified metrics (e.g. `%`, `ms`, user scale).
* Average latency: **< 50 milliseconds**.
* Ensures the platform **never returns a 500 Internal Server Error** during student submissions.

---

## 5. Structured Extraction System

Along with the ATS evaluation scores, the service extracts all core components of the resume and packages them into an `extractedData` object:

| Category | Model | Extracted Fields |
| :--- | :--- | :--- |
| **Candidate Info** | `CandidateInfo` | `name`, `email`, `phone`, `linkedin`, `github`, `portfolio` |
| **Education** | `EducationItem` | `institution`, `degree`, `year`, `score` (CGPA / %) |
| **Experience** | `ExperienceItem` | `company`, `role`, `duration`, `highlights` |
| **Projects** | `ProjectItem` | `title`, `techStack`, `description`, `link` |
| **Skills** | `CategorizedSkills` | `languages`, `frameworks`, `toolsAndDatabases`, `coreSubjects` |
| **Certifications**| `List<String>` | Professional certifications & verified achievements |
| **Full Text** | `String` | Complete raw text parsed from the PDF document |

---

## 6. Prerequisites & Configuration

### Prerequisites
- **Java 21 or Java 22**
- **Maven Wrapper** (included as `mvnw` and `mvnw.cmd`)

### Port & Upload Configuration
Configured in `src/main/resources/application.properties`:

```properties
spring.application.name=ResumeService
server.port=8083

# Maximum upload limits (REQ-RES-2: 2 MB limit)
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
spring.servlet.multipart.enabled=true
file.upload-dir=uploads/resumes

# AI Engine Configuration (Groq / OpenAI compatible)
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.api-key=${GROQ_API_KEY:}
spring.ai.openai.chat.options.model=${GROQ_MODEL:openai/gpt-oss-120b}
spring.ai.openai.chat.options.temperature=0.3
spring.ai.openai.chat.options.max-tokens=4096
```

### Database Options

#### Option A: Embedded H2 Database (Default, Zero-Setup)
Runs immediately in-memory with automatic schema creation:
```properties
spring.datasource.url=jdbc:h2:mem:resumedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
```

#### Option B: MySQL 8 (Production Deployment per SRS DB-1)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resume_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## 7. API Reference & Usage

### Quick Access URLs Summary (`http://localhost:8083`)

| # | Action | HTTP Method | Complete URL | Headers / Body |
| :---: | :--- | :---: | :--- | :--- |
| **1** | **Upload & Analyze Resume** | `POST` | `http://localhost:8083/api/v1/resumes/analyze` | `multipart/form-data`<br>• `file`: (PDF file, max 2 MB)<br>• `studentId`: `101`<br>• `targetRole`: `Software Development Engineer (SDE)` |
| **2** | **Upload Alias** | `POST` | `http://localhost:8083/api/v1/resumes/upload` | *(Same as above)* |
| **3** | **Get Student History** | `GET` | `http://localhost:8083/api/v1/resumes/history/{studentId}` | *None (open in browser or Postman)* |
| **4** | **Get Latest Analysis** | `GET` | `http://localhost:8083/api/v1/resumes/latest/{studentId}` | *Consumed by AI Interview Service* |
| **5** | **Get Analysis by ID** | `GET` | `http://localhost:8083/api/v1/resumes/{resumeId}` | *None* |
| **6** | **Get Supported Roles** | `GET` | `http://localhost:8083/api/v1/resumes/target-roles` | *None (open directly in browser)* |
| **7** | **Service Health Check** | `GET` | `http://localhost:8083/api/v1/resumes/health` | *None (open directly in browser)* |
| **8** | **H2 Database Web Console**| `GET` | `http://localhost:8083/h2-console` | JDBC URL: `jdbc:h2:mem:resumedb`<br>User: `sa`, Password: *(empty)* |

---

### How to Test via Postman:
1. Set HTTP Method to `POST`.
2. Enter URL: `http://localhost:8083/api/v1/resumes/analyze`
3. Under the **Body** tab, select **form-data**:
   - `file`: Hover on key, change type dropdown from *Text* to *File* $\rightarrow$ Select your PDF file.
   - `studentId`: Enter `101` (Text).
   - `targetRole`: Enter `Software Development Engineer (SDE)` (Text).
4. Click **Send**.

---

### 1. Upload & Analyze Resume
Uploads a PDF resume, extracts all text and structured entities, evaluates ATS compatibility against the target role, saves to the database, and returns the unified analysis.

* **URL**: `/api/v1/resumes/analyze` (or `/api/v1/resumes/upload`)
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### Parameters:
| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :--- | :--- |
| `file` | MultipartFile | **Yes** | — | Candidate PDF file (maximum 2 MB). |
| `studentId`| Long | No | `1` | ID of the student. Taken from JWT in API Gateway. |
| `targetRole`| String | No | `Software Development Engineer (SDE)` | Target engineering role. |

#### Example cURL:
```bash
curl -X POST http://localhost:8083/api/v1/resumes/analyze \
  -F "file=@/path/to/resume.pdf;type=application/pdf" \
  -F "studentId=101" \
  -F "targetRole=Software Development Engineer (SDE)"
```

---

### 2. Get Student Analysis History
Retrieves all historical resume analysis attempts for a given student ID.

* **URL**: `/api/v1/resumes/history/{studentId}`
* **Method**: `GET`

#### Example cURL:
```bash
curl http://localhost:8083/api/v1/resumes/history/101
```

---

### 3. Get Latest Resume Analysis
Retrieves the most recent analyzed resume for a student. Consumed by the **AI Interview Service** to personalize technical and behavioral questions.

* **URL**: `/api/v1/resumes/latest/{studentId}`
* **Method**: `GET`

#### Example cURL:
```bash
curl http://localhost:8083/api/v1/resumes/latest/101
```

---

### 4. Get Analysis by Resume ID
Retrieves a specific analysis record by its primary key ID.

* **URL**: `/api/v1/resumes/{resumeId}`
* **Method**: `GET`

#### Example cURL:
```bash
curl http://localhost:8083/api/v1/resumes/1
```

---

### 5. List Supported Target Roles
Returns the list of all supported target job roles and their skill requirements.

* **URL**: `/api/v1/resumes/target-roles`
* **Method**: `GET`

#### Supported Roles Catalog:
1. `Software Development Engineer (SDE)`
2. `Backend Engineer`
3. `Frontend Engineer`
4. `Full Stack Engineer`
5. `Data Scientist / Machine Learning Engineer`
6. `DevOps & Cloud Engineer`
7. `Mobile App Developer (Android/iOS)`
8. `QA & Test Automation Engineer`

---

### 6. Service Health Check
* **URL**: `/api/v1/resumes/health`
* **Method**: `GET`

---

## 8. Complete Response Schema & Contract

### Success Response (`200 OK`)
```json
{
  "resumeId": 1,
  "studentId": 101,
  "fileName": "nitin_resume.pdf",
  "fileSize": 145280,
  "targetRole": "Software Development Engineer (SDE)",

  // -----------------------------------------------------------
  // 1. ATS EVALUATION & SCORING METRICS
  // -----------------------------------------------------------
  "atsScore": 82,
  "formatScore": 88,
  "experienceScore": 80,
  "extractedSkills": [
    "Java",
    "Python",
    "Data Structures",
    "Algorithms",
    "SQL",
    "MySQL",
    "Spring Boot",
    "Docker",
    "Git",
    "REST APIs"
  ],
  "matchedSkills": [
    "Data Structures",
    "Algorithms",
    "Java",
    "SQL",
    "Git"
  ],
  "missingSkills": [
    "C++",
    "System Design",
    "Computer Networks"
  ],
  "suggestions": [
    "Add target role keywords: Include essential skills for Software Development Engineer (SDE), specifically: C++, System Design.",
    "Quantify your achievements: Use the X-Y-Z formula (Accomplished [X] measured by [Y] by doing [Z]). For example: 'Optimized database queries, reducing response latency by 35%'.",
    "Ensure professional contact details are prominently placed at the top, including your GitHub and LinkedIn profile URLs."
  ],
  "summaryFeedback": "Strong resume profile for Software Development Engineer (SDE) with an ATS score of 82/100. Good foundation in Java, Data Structures, and backend development. Adding System Design and containerization experience will significantly boost placement shortlisting.",
  "analysisSource": "AI_POWERED",
  "uploadedAt": "2026-10-04T12:15:36.730Z",

  // -----------------------------------------------------------
  // 2. COMPLETE EXTRACTED RESUME DATA (SAVED TO DATABASE)
  // -----------------------------------------------------------
  "extractedData": {
    "candidateInfo": {
      "name": "Nitin Tiwari",
      "email": "nitin@example.com",
      "phone": "+91 9876543210",
      "linkedin": "linkedin.com/in/nitintiwari",
      "github": "github.com/nitintiwari",
      "portfolio": "https://nitintiwari.dev"
    },
    "education": [
      {
        "institution": "Institute of Engineering and Technology (IET)",
        "degree": "B.Tech in Information Technology",
        "year": "2026",
        "score": "8.5 CGPA"
      }
    ],
    "experience": [
      {
        "company": "TechCorp",
        "role": "Software Development Intern",
        "duration": "Jun 2025 - Aug 2025",
        "highlights": [
          "Architected and implemented high-throughput REST APIs using Spring Boot and MySQL.",
          "Optimized database queries, reducing response latency by 42% across 50,000+ daily requests."
        ]
      }
    ],
    "projects": [
      {
        "title": "Interview IQ - AI Placement Preparation Platform",
        "techStack": [
          "Java",
          "Spring Boot",
          "React.js",
          "MySQL",
          "Docker"
        ],
        "description": "AI placement preparation platform with sandboxed code execution and real-time mock interviews.",
        "link": "https://github.com/nitintiwari/interview-iq"
      }
    ],
    "skills": {
      "languages": [
        "Java",
        "Python",
        "SQL",
        "C++"
      ],
      "frameworks": [
        "Spring Boot",
        "React.js"
      ],
      "toolsAndDatabases": [
        "MySQL",
        "Docker",
        "Git",
        "Postman"
      ],
      "coreSubjects": [
        "Data Structures",
        "Algorithms",
        "OOP",
        "DBMS",
        "Operating Systems"
      ]
    },
    "certifications": [
      "AWS Certified Cloud Practitioner",
      "Oracle Certified Java Associate"
    ],
    "rawText": "Nitin Tiwari\nEmail: nitin@example.com | Phone: +91 9876543210\nLinkedIn: linkedin.com/in/nitintiwari | GitHub: github.com/nitintiwari\n\nEDUCATION\nB.Tech in Information Technology - 2026\nInstitute of Engineering and Technology (IET)\n..."
  }
}
```

---

### Error Responses (Standardized per REQ-RES-2)

#### When a non-PDF file is submitted:
```json
{
  "timestamp": "2026-10-04T12:15:36.120Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Unsupported file format"
}
```

#### When file size exceeds 2 MB:
```json
{
  "timestamp": "2026-10-04T12:15:36.120Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File too large (maximum 2 MB)"
}
```

---

## 9. Database Architecture

Strictly adheres to SRS Section 6.1, Table 6-1:

```sql
CREATE TABLE resumes (
    resume_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_size BIGINT,
    target_role VARCHAR(150) NOT NULL,
    ats_score INT NOT NULL,
    format_score INT,
    experience_score INT,
    extracted_skills TEXT,
    matched_skills TEXT,
    missing_skills TEXT,
    suggestions TEXT,
    summary_feedback TEXT,
    analysis_source VARCHAR(50),
    extracted_data LONGTEXT,
    uploaded_at TIMESTAMP NOT NULL,
    INDEX idx_resumes_student_id (student_id)
);
```

---

## 10. Inter-Service Integration

### 1. Consumed by AI Interview Service (REQ-INT-3, REQ-INT-15)
When a student starts an AI mock interview:
```
GET http://resume-service:8083/api/v1/resumes/latest/{studentId}
```
The Interview Service reads `extractedSkills`, `extractedData.projects`, and `extractedData.experience` to dynamically generate tailored technical and behavioral questions.

### 2. Consumed by Analytics / Roadmap Service (REQ-DSH-1, REQ-RMP-1, Table 3-4)
On every successful analysis, `ResumeEventPublisher` broadcasts a `ResumeAnalyzed` event:
```json
{
  "eventType": "ResumeAnalyzed",
  "studentId": 101,
  "resumeId": 1,
  "targetRole": "Software Development Engineer (SDE)",
  "atsScore": 82,
  "formatScore": 88,
  "experienceScore": 80,
  "extractedSkills": ["Java", "Spring Boot", "MySQL", "Docker"],
  "missingSkills": ["C++", "System Design"],
  "timestamp": "2026-10-04T12:15:36.730Z"
}
```
The Analytics Service consumes this event to update the student's performance profile and adjust their personalized preparation roadmap.

---

## 11. Running & Verification

### Running the Service

#### Windows (PowerShell):
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
cd c:\Users\ACER\Downloads\InteliInterview\ResumeService

# (Optional: set Groq API key; fallback engine activates automatically if omitted)
$env:GROQ_API_KEY = "gsk_your_groq_api_key_here"

.\mvnw.cmd spring-boot:run
```

#### Linux / macOS:
```bash
export GROQ_API_KEY="gsk_your_groq_api_key_here"
./mvnw spring-boot:run
```

The service will start on **`http://localhost:8083`**.

---

### Executing Tests
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
.\mvnw.cmd test
```

All 10 unit and integration tests validate PDF parsing, 2 MB size enforcement, fallback ATS analysis, and end-to-end API upload and DB retrieval.

---

## 12. Troubleshooting & FAQ

**Q: Do I need an active internet connection or a paid AI API key to test the service?**  
**A:** No. If `GROQ_API_KEY` is not provided, the service seamlessly defaults to the **Rule-Based ATS Keyword & Heuristic Engine**, extracting contact info, skills, education, projects, and calculating scores with zero downtime and sub-50ms latency.

**Q: What happens if a student uploads an image or scanned document?**  
**A:** Scanned PDFs with no readable text are trapped by `PdfParserService`, returning an instructive error prompting the student to provide a text-based PDF (conforming to SRS Assumption A-2).

**Q: Where are the uploaded PDF files saved?**  
**A:** Files are stored on the server filesystem in `uploads/resumes/` with UUID-prefixed filenames to prevent collisions.

**Q: How do other services access student data securely?**  
**A:** Services query by `student_id` extracted from the validated JWT token by the API Gateway (conforming to SRS C-2, C-3, SEC-5).
