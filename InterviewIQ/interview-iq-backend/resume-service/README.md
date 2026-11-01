# resume-service (port 8082)

PDF resume upload, ATS analysis (Groq AI + rule-based fallback), history. Contract: `docs/api-contracts/resume.md`.
AI member ka original service hai; integration ke liye yeh badla gaya (details `README-ai-member.md` ke upar likhe note mein).

## Run
Pehle MySQL. Phir `.\mvnw.cmd spring-boot:run` (JDK 21). DB password `src/main/resources/application.properties` mein (ya env `DB_PASSWORD`).
Optional AI: `$env:GROQ_API_KEY = "gsk_..."` (nahi diya toh rule-based analyzer chalta hai).

## Integration changes (original se)
- Port 8083 -> **8082** (8083 coding-service ka hai). Package `com.interviewiq.resume`.
- Paths `/api/v1/resumes/**` -> **`/resume/**`** (gateway route ke saath match).
- `studentId` request param hata diya: user **X-User-Id** header se (gateway ke verified JWT se). Warna koi bhi doosre ka resume dekh sakta tha.
- Service-to-service ke liye `/internal/resume/latest/{studentId}` aur `/internal/resume/history/{studentId}` (gateway pe route nahi).
- H2 in-memory default -> **MySQL** (`resume_db`). H2 ab sirf tests mein.
- `api-key=${API_KEY}` (bina default, key na ho toh startup crash) -> `${GROQ_API_KEY:}`.
- CORS service se hata diya (gateway sambhalta hai). Locked/scanned PDF ab 400 (pehle 500). 500 mein internal error text nahi jata.
- `uploads/` .gitignore mein (resume PDFs personal data hain).

## Resume details direct edit (Step 5b)
- `PUT /resume/details`: latest analysis row ki `extracted_data` seedha update (nayi row nahi). Naye columns: `skill_match_score`, `last_edited_at` (Hibernate apne aap add karta hai).
- `GET /resume/roadmap`: role ki skills ka checklist (done/baki, priority CORE/SECONDARY/TOOL).
- Skill match sirf structured details se nikalta hai (skills lists, projects, experience, certifications), rawText se nahi, taaki student skill hataye toh match bhi hate.
- ATS score PDF se aaya tha, edit se nahi badalta. UI mein ATS score aur skill match alag dikhao.
- Trade-off: "har upload ek nayi row, kabhi overwrite nahi" (SRS REQ-RES-7) upload pe ab bhi sach hai; edits sirf latest row pe in-place hain, toh edits ki history nahi rehti (sirf current state).
