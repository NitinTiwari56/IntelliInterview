# API Gateway (port 8080)  -  frontend ka SIRF yahi URL

Frontend base URL: `http://localhost:8080`
Frontend kisi aur port (8081, 8084, ...) ko direct call NAHI karega.

## Routes
| Path | Service | Token chahiye? |
|---|---|---|
| POST /auth/register, POST /auth/login | auth-service (8081) | Nahi (public) |
| /auth/** (baaki) | auth-service | Haan |
| /resume/** | resume-service (8082) | Haan |
| /coding/** | coding-service (8083) | Haan |
| /aptitude/** | aptitude-service (8084) | Haan |
| /interview/** | interview-service (8085) | Haan |
| /analytics/** | analytics-service (8086) | Haan |
| /**/admin/** | koi bhi | Haan + role ADMIN |

Path strip nahi hota: `/aptitude/tests` gateway se service ke `/aptitude/tests` pe jata hai.
Internal services (aptitude-question-service 8091, llm-gateway 8090) ka koi route nahi hai.

## Token kaise bhejna hai
Login ke baad mila `token` har request mein:
```
Authorization: Bearer <token>
```

## Gateway ke errors (format wahi: status, message, timestamp)
| Status | Kab |
|---|---|
| 401 | Header missing / token invalid / expired. Frontend: user ko login page pe bhejo |
| 403 | Student ne /admin/ path hit kiya |
| 5xx | Downstream service band hai |

## Downstream services ke liye (backend)
Gateway verified user ki info headers mein bhejta hai. Services JWT dobara parse nahi karengi:
- `X-User-Id`   (userId)
- `X-User-Email`
- `X-User-Role` (STUDENT / ADMIN)

Client ke bheje `X-User-*` headers gateway hata deta hai, toh spoof nahi ho sakte.
IMPORTANT: services direct (gateway ke bina) public network pe expose nahi honi chahiye.
Spring mein: `@RequestHeader("X-User-Id") Long userId`

## Config
| Env variable | Default |
|---|---|
| JWT_SECRET | auth-service wala hi (same hona zaroori) |
| CORS_ALLOWED_ORIGINS | http://localhost:5173,http://localhost:3000 |
| AUTH_SERVICE_URL, RESUME_SERVICE_URL, ... | http://localhost:<port> |
