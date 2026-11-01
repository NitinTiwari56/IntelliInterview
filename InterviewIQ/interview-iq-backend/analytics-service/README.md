# analytics-service (port 8086)

Dashboard + weak areas + personalized roadmap. Contract: `docs/api-contracts/analytics.md`.
AI member ka RoadMapService hai, yahan `analytics-service` naam se integrate kiya gaya. Original README: `README-ai-member.md` (paths/ports ab purane hain).

## Ye data kahan se laata hai (REST, sirf internal endpoints)
| Module | Endpoint |
|---|---|
| Resume | `GET :8082/internal/resume/{id}/summary` |
| Interview | `GET :8085/internal/interview/{id}/summary` |
| Coding | `GET :8083/internal/coding/{id}/summary` |
| Aptitude | `GET :8084/internal/aptitude/{id}/summary` |

Ye `/internal/**` paths gateway pe route nahi hain, toh browser inhe nahi chhoo sakta. Koi service band ya "no data" (404) ho toh
dashboard baaki se bana leta hai (REQ-DSH-7).

## Run
Pehle MySQL, phir baaki services (jitni chalu rahengi utna data dikhega). `.\mvnw.cmd spring-boot:run` (JDK 21).
DB password `src/main/resources/application.properties` mein. Optional AI: `$env:GROQ_API_KEY = "gsk_..."`.

## Integration changes (original se)
- Paths `/api/dashboard/{studentId}`, `/api/roadmap/{studentId}` -> `/analytics/dashboard`, `/analytics/roadmap`; **studentId URL se hata**,
  user `X-User-Id` header se (warna koi bhi kisi ka dashboard dekh leta).
- Doosri services ke URLs/ports/paths hamare services ke hisaab se (original ke paths wahan the hi nahi).
  Teen services mein `/internal/*/summary` endpoints add kiye.
- H2 -> **MySQL** (`analytics_db`), `api-key=${API_KEY}` -> `${GROQ_API_KEY:}`, CORS hata diya, 500 mein internal text nahi.
- **Kafka nahi use ho raha** (dependency hai, par listener band hai, broker ke bina chalta hai). Original mein roadmap tab refresh hota jab Kafka event aata;
  ab cached roadmap 30 min baad apne aap refresh hota hai (`analytics.roadmap.max-age-minutes`) aur "Refresh" button hai.
- Summary DTOs mein unknown fields ignore.

## Limits
- Dashboard har request pe 4 services ko call karta hai (2.5 sec connect timeout). Koi service band ho toh thoda slow.
- `GET /analytics/dashboard` bhi profile row upsert karta hai (pehli baar dashboard aur roadmap ek saath mat bulao).
