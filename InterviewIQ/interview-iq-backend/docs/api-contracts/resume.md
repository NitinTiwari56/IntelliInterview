# 4. Resume APIs (gateway: http://localhost:8080)

Sab endpoints mein header: `Authorization: Bearer <token>`
STATUS: backend testing chal rahi hai, field names thode badal sakte hain.

Rules:
- Sirf **PDF**, max **2 MB**. Text-wali PDF chahiye (scanned image/password-wali PDF reject hoti hai, `400`).
- Request mein `studentId` kabhi mat bhejo, user token se pehchana jata hai.
- Analysis 1-5 sec le sakti hai (AI), loading dikhao.
- Har upload ek **nayi** analysis banata hai (purani overwrite nahi hoti), toh history mein progress dikhta hai.

## GET /resume/target-roles
Dropdown ke liye. Select kiya hua `roleName` upload ke saath bhejo.
```json
[ { "roleName": "Software Development Engineer (SDE)", "description": "...",
    "coreSkills": ["Data Structures", "..."], "secondarySkills": ["..."], "toolsAndFrameworks": ["..."] } ]
```
Roles: SDE, Backend Engineer, Frontend Engineer, Full Stack Engineer, Data Scientist / Machine Learning Engineer, DevOps & Cloud Engineer, Mobile App Developer (Android/iOS), QA & Test Automation Engineer.

## POST /resume/analyze   (multipart/form-data)
| Field | Type | Required |
|---|---|---|
| `file` | PDF file | haan |
| `targetRole` | text (roleName) | nahi (default SDE) |

Frontend (axios): `const fd = new FormData(); fd.append("file", file); fd.append("targetRole", role);`
`Content-Type` header khud mat set karo (browser boundary ke saath lagata hai).
```json
{
  "resumeId": 1, "studentId": 7, "fileName": "resume.pdf", "fileSize": 145280,
  "targetRole": "Software Development Engineer (SDE)",
  "atsScore": 82, "formatScore": 88, "experienceScore": 80, "skillMatchScore": 64, "lastEditedAt": null,
  "extractedSkills": ["Java", "SQL"], "matchedSkills": ["Java"], "missingSkills": ["System Design"],
  "suggestions": ["..."], "summaryFeedback": "...",
  "analysisSource": "AI_POWERED",
  "uploadedAt": "2026-10-05T12:15:36Z",
  "extractedData": {
    "candidateInfo": { "name": "", "email": "", "phone": "", "linkedin": "", "github": "", "portfolio": "" },
    "education": [ { "institution": "", "degree": "", "year": "", "score": "" } ],
    "experience": [ { "company": "", "role": "", "duration": "", "highlights": [""] } ],
    "projects": [ { "title": "", "techStack": [""], "description": "", "link": "" } ],
    "skills": { "languages": [], "frameworks": [], "toolsAndDatabases": [], "coreSubjects": [] },
    "certifications": [],
    "rawText": "..."
  }
}
```
- `analysisSource`: `AI_POWERED` ya `RULE_BASED` (AI na chale toh bhi result milta hai).
- Scores 0-100.
Errors: `400` galat file type / 2 MB se bada / scanned ya locked PDF, `401` token nahi, `500` kuch toot gaya (dobara try karo)

## GET /resume/history
```json
{ "studentId": 7, "totalAnalyses": 3, "averageAtsScore": 74.3, "latestScore": 82,
  "history": [ { "resumeId": 3, "fileName": "resume.pdf", "targetRole": "...", "atsScore": 82, "uploadedAt": "..." } ] }
```
Naya pehle. Progress chart ke liye `history[].atsScore`.

## GET /resume/latest
Meri sabse latest analysis (upar wali shape). Nahi hai toh `404`.

## GET /resume/{resumeId}
Meri ek analysis. Doosre ki id dene pe `404`.


## PUT /resume/details   (resume ki details direct edit)
Latest analysis ki details **usi record mein** update hoti hain (nayi row nahi banti). Jo field nahi bheji wo jaisi hai waisi rehti hai.
Jo field bheji wo **poori replace** hoti hai, toh frontend poori list bheje (jaise skills ka poora object).
Edit hone ke turant baad skill match dobara nikalta hai (bina AI, bina PDF upload).
```json
{
  "skills": { "languages": ["Java", "Python", "C++"], "frameworks": ["Spring Boot"],
              "toolsAndDatabases": ["MySQL", "Git"], "coreSubjects": ["Data Structures", "DBMS"] },
  "projects": [ { "title": "Interview IQ", "techStack": ["Java", "Spring Boot"], "description": "...", "link": "" } ],
  "certifications": ["AWS Cloud Practitioner"],
  "targetRole": "Backend Engineer"
}
```
Allowed fields: `candidateInfo`, `education`, `experience`, `projects`, `skills`, `certifications`, `targetRole` (sab optional).
Response: poori analysis (upar wali shape) naye `skillMatchScore`, `matchedSkills`, `missingSkills`, `lastEditedAt` ke saath.
- `atsScore`, `formatScore`, `experienceScore` **nahi badalte** (wo PDF se aaye the). UI mein dono alag dikhao:
  "ATS score (PDF)" aur "Skill match (live)".
Errors: `404` pehle koi resume upload nahi hui, `400` bahut zyada items (skills max 60 per list, projects/education/experience max 20, certifications max 30)

## GET /resume/roadmap
Target role ki skills ka checklist. Edit ke baad dobara call karo, done wali skills tick ho jati hain.
```json
{
  "resumeId": 4, "targetRole": "Backend Engineer", "skillMatchScore": 64,
  "coveredCount": 12, "totalCount": 25, "atsScore": 82, "lastEditedAt": "2026-10-08T10:00:00Z",
  "items": [
    { "skill": "Microservices", "priority": "CORE", "done": false, "hint": "Core skill for this role. Learn it, add it to your Skills, and prove it in a project." },
    { "skill": "Java", "priority": "CORE", "done": true, "hint": "Covered in your resume." }
  ]
}
```
- `priority`: `CORE` (60% score) > `SECONDARY` (30%) > `TOOL` (10%)
- `items` pehle **baki** (CORE, phir SECONDARY, phir TOOL), uske baad done wali.
- Progress bar: `coveredCount / totalCount`, aur `skillMatchScore`.
- Ye resume-wala roadmap hai. Aptitude/coding/interview ko milake poora personalized roadmap baad mein Analytics se aayega.

## Error format
`{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }`
Frontend `message` dikhaye.
