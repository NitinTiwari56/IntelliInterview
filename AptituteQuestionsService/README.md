# Aptitude Questions Service (`AptituteQuestionsService`)

A high-performance microservice designed for **Interview Preparation Platforms**. It generates randomized, structured multiple-choice aptitude questions on demand for other backend services (e.g., test evaluation services, candidate portal backends, quiz engines).

Powered by **Groq Cloud AI** (`llama-3.3-70b-versatile` via OpenAI-compatible Spring AI) with an automatic zero-latency **Curated Question Bank Fallback**.

---

## Table of Contents
1. [What is this Service For?](#1-what-is-this-service-for)
2. [How it Works (Architecture & Flow)](#2-how-it-works-architecture--flow)
3. [Setup & Configuration](#3-setup--configuration)
4. [How to Use (API Reference)](#4-how-to-use-api-reference)
5. [What to Expect (Response Schema)](#5-what-to-expect-response-schema)
6. [Calling from Other Backend Services](#6-calling-from-other-backend-services)
7. [Troubleshooting & FAQ](#7-troubleshooting--faq)

---

## 1. What is this Service For?

In an interview preparation platform, candidates need fresh, randomized assessment tests. This service acts as the **central question engine**:

- **Receives requests** from your main backend (e.g., test creation service, assessment orchestrator).
- **Generates a batch of aptitude questions** (default 20, customizable).
- **Evenly distributes questions** across the core aptitude domains:
  - 🔢 **Quantitative Aptitude** (Time & Work, Profit & Loss, Percentages, Ratio, Speed/Distance, Probability, etc.)
  - 🧠 **Logical Reasoning** (Blood Relations, Coding-Decoding, Number Series, Syllogisms, Seating Arrangement, etc.)
  - 📖 **Verbal Ability** (Synonyms, Antonyms, Sentence Correction, Spotting Errors, Reading Comprehension, etc.)
  - 📊 **Data Interpretation** (Tables, Bar Charts, Pie Charts, Caselet Analysis)
- **Delivers a clean, structured JSON payload** containing question text, 4 multiple-choice options, correct answer, zero-based correct option index, explanation, and marks.

---

## 2. How it Works (Architecture & Flow)

The service implements a **Dual-Engine Strategy** with automatic resilience:

```
[ Your Other Backend Service ]
              │
              ▼
   POST /api/v1/aptitude/generate
              │
    ┌─────────┴─────────┐
    │  Groq API Key     │
    │  Configured?      │
    └─────────┬─────────┘
        YES   │             NO / Error / Rate Limit
              ├────────────────────────┐
              ▼                        ▼
    ┌───────────────────────────┐    ┌──────────────────────────┐
    │      Groq AI Engine       │    │  Curated Question Bank   │
    │     (LPU Ultra-Fast)      │    │ (40+ Pre-built Questions)│
    │     openai/gpt-oss-120b   │    │                          │
    │                           │    │ • Random Selection (20)  │
    │ • Dynamic puzzles         │    │ • Option Shuffling       │
    │ • Fresh scenarios         │    │ • Zero Latency (<5ms)    │
    │ • Temperature=0.7         │    │ • 100% High Availability │
    └─────────────┬─────────────┘    └─────────────┬────────────┘
                  │                                │
                  └─────────────┬──────────────────┘
                                ▼
                  Returns Standard JSON Batch Response
```

1. **Groq AI Engine (Primary)**:
   - Uses Groq's high-speed inference on `openai/gpt-oss-120b` (or `llama-3.1-8b-instant`).
   - Generates brand new questions, scenarios, numbers, and explanations every single time.
   - Response time is typically **1–2 seconds**.
2. **Curated Question Bank (Zero-Downtime Fallback)**:
   - Contains a vetted repository of quantitative, logical, verbal, and data interpretation questions.
   - Applies `Collections.shuffle()` on both question selection and the 4 options (automatically re-indexing `correctOptionIndex`).
   - If Groq key is missing, network is down, or Groq hits rate limits, the service **never fails with 500**—it instantly delivers questions from the bank.

---

## 3. Setup & Configuration

### Prerequisites
- **Java 21 or Java 22**
- **Maven Wrapper** (included as `mvnw` / `mvnw.cmd`)

### Step 1: Configure Your Groq API Key

Grab a free key from the [Groq Console](https://console.groq.com/keys).

#### Option A: Set Environment Variable (Recommended)
- **Windows (PowerShell)**:
  ```powershell
  $env:GROQ_API_KEY="gsk_your_groq_api_key_here"
  ```
- **Windows (CMD)**:
  ```cmd
  set GROQ_API_KEY=gsk_your_groq_api_key_here
  ```
- **Linux / macOS**:
  ```bash
  export GROQ_API_KEY="gsk_your_groq_api_key_here"
  ```

#### Option B: In `src/main/resources/application.properties`
```properties
server.port=8082

spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.api-key=${GROQ_API_KEY:gsk_your_groq_api_key_here}
spring.ai.openai.chat.options.model=openai/gpt-oss-120b
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=4096
```

### Step 2: Run the Service
- **Windows**:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Java\jdk-22"  # (Adjust if needed)
  .\mvnw.cmd spring-boot:run
  ```
- **Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```
The service will start on port **`8082`**.

---

## 4. How to Use (API Reference)

### Endpoint 1: Generate Questions (`POST` - Recommended)

**URL**: `http://localhost:8082/api/v1/aptitude/generate`  
**Method**: `POST`  
**Headers**: `Content-Type: application/json`

#### Request Body Options:

**Standard 20 Random Questions (Default):**
```json
{
  "count": 20
}
```

**Custom Filtered Test:**
```json
{
  "count": 20,
  "category": "ALL",
  "difficulty": "MIXED",
  "source": "AUTO",
  "topics": ["Time and Work", "Profit and Loss", "Blood Relations"]
}
```

#### Request Parameters Reference:
| Field | Type | Default | Accepted Values | Description |
|---|---|---|---|---|
| `count` | Integer | `20` | `1` to `100` | Number of questions requested. |
| `category` | String | `"ALL"` | `"ALL"`, `"QUANTITATIVE"`, `"LOGICAL_REASONING"`, `"VERBAL_ABILITY"`, `"DATA_INTERPRETATION"` | Filter questions by domain. |
| `difficulty` | String | `"MIXED"` | `"MIXED"`, `"EASY"`, `"MEDIUM"`, `"HARD"` | Desired difficulty level. |
| `topics` | Array[String] | `null` | e.g. `["Percentages", "Averages"]` | Optional focus topics. |
| `source` | String | `"AUTO"` | `"AUTO"`, `"AI"`, `"BANK"` | `"AUTO"` = Try Groq first, fallback to bank.<br>`"AI"` = Force Groq AI.<br>`"BANK"` = Force instant curated bank. |

---

### Endpoint 2: Generate Questions via `GET` (Quick Browser / Postman Test)

**URL**:
```text
http://localhost:8082/api/v1/aptitude/generate?count=20
```

Query parameters supported: `count`, `category`, `difficulty`, `source`.

---

### Endpoint 3: Health & Diagnostics Check (`GET`)

**URL**:
```text
http://localhost:8082/api/v1/aptitude/health
```

**Use Case**: Other microservices or Kubernetes liveness/readiness probes can check this endpoint to verify Groq AI status and question bank availability.

---

## 5. What to Expect (Response Schema)

Every request produces a clean, consistent response payload.

### Successful Response Format (`200 OK`)

```json
{
  "success": true,
  "message": "Successfully generated 20 aptitude questions via Groq AI.",
  "totalQuestions": 20,
  "source": "AI_GENERATED",
  "categoryDistribution": {
    "QUANTITATIVE": 6,
    "LOGICAL_REASONING": 5,
    "VERBAL_ABILITY": 5,
    "DATA_INTERPRETATION": 4
  },
  "questions": [
    {
      "id": "APT-Q1",
      "category": "QUANTITATIVE",
      "topic": "Time and Work",
      "difficulty": "MEDIUM",
      "question": "A can complete a work in 12 days and B can complete it in 18 days. If they work together, in how many days will they finish the work?",
      "options": [
        "7.2 days",
        "8.5 days",
        "6.4 days",
        "9.0 days"
      ],
      "correctOptionIndex": 0,
      "correctAnswer": "7.2 days",
      "explanation": "Work done by A in 1 day = 1/12. Work done by B in 1 day = 1/18. Together in 1 day = 1/12 + 1/18 = 5/36. Total time = 36/5 = 7.2 days.",
      "marks": 1
    }
  ]
}
```

### Response Field Descriptions:

| Field | Type | Description |
|---|---|---|
| `success` | Boolean | `true` if request completed successfully. |
| `message` | String | Informational message including whether AI or Bank served the request. |
| `totalQuestions` | Integer | Count of questions returned in `questions` list. |
| `source` | String | `"AI_GENERATED"` (Groq) or `"QUESTION_BANK"` (Fallback). |
| `categoryDistribution` | Object | Breakdown of question count per category. |
| `questions` | Array | List of question objects. |
| ↳ `id` | String | Unique question ID (e.g. `APT-Q1` or UUID-backed). |
| ↳ `category` | String | `QUANTITATIVE`, `LOGICAL_REASONING`, `VERBAL_ABILITY`, or `DATA_INTERPRETATION`. |
| ↳ `topic` | String | Sub-topic name (e.g., `Time and Work`, `Blood Relations`). |
| ↳ `difficulty` | String | `EASY`, `MEDIUM`, or `HARD`. |
| ↳ `question` | String | The problem statement. |
| ↳ `options` | Array[String] | Array of exactly 4 choices. |
| ↳ `correctOptionIndex` | Integer | Zero-based index (`0`, `1`, `2`, or `3`) matching the correct option. |
| ↳ `correctAnswer` | String | Exact text of the correct choice. |
| ↳ `explanation` | String | Step-by-step mathematical/logical reasoning explaining the answer. |
| ↳ `marks` | Integer | Marks assigned to the question (default `1`). |

---

## 6. Calling from Other Backend Services

Here is how your other backend services can consume this API:

### Node.js / Express (Axios)
```javascript
const axios = require('axios');

async function getAptitudeTest() {
  const response = await axios.post('http://localhost:8082/api/v1/aptitude/generate', {
    count: 20,
    category: 'ALL',
    difficulty: 'MIXED'
  });

  const { totalQuestions, questions } = response.data;
  console.log(`Received ${totalQuestions} questions.`);
  return questions;
}
```

### Python / FastAPI / Django (Requests)
```python
import requests

def fetch_aptitude_test():
    url = "http://localhost:8082/api/v1/aptitude/generate"
    payload = {"count": 20, "category": "ALL", "difficulty": "MIXED"}
    
    response = requests.post(url, json=payload)
    data = response.json()
    return data["questions"]
```

### Java / Spring Boot (`RestClient`)
```java
RestClient restClient = RestClient.create();

QuestionBatchResponse response = restClient.post()
    .uri("http://localhost:8082/api/v1/aptitude/generate")
    .contentType(MediaType.APPLICATION_JSON)
    .body(Map.of("count", 20))
    .retrieve()
    .body(QuestionBatchResponse.class);
```

---

## 7. Troubleshooting & FAQ

### Q1: What happens if Groq API key is not provided?
The service starts normally without errors. Requests to `/api/v1/aptitude/generate` automatically use the internal Curated Question Bank. Your frontend / platform will never see an outage.

### Q2: What happens if Groq hits free-tier rate limit (TPM/RPM)?
The service catches the exception gracefully, logs a warning, and immediately serves randomized questions from the bank. The response will include:
```json
"source": "QUESTION_BANK",
"message": "AI generation unavailable (...); fell back to question bank."
```

### Q3: How do I change the Groq model?
In `application.properties`, set:
```properties
# High quality reasoning (default)
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile

# Ultra-fast sub-second generation
# spring.ai.openai.chat.options.model=llama-3.1-8b-instant
```

### Q4: How do I run tests?
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
.\mvnw.cmd test
```
All tests run with an embedded mock context and do not require internet or an active API key to pass.
