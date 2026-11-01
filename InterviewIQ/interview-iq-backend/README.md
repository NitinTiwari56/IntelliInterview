# Interview IQ - Backend

AI-Driven Placement Preparation Platform. Backend = Spring Boot microservices + MySQL (database per service).

## Folder structure
```
interview-iq-backend/
├── docker-compose.yml          # MySQL container
├── infra/mysql/init.sql        # creates all service schemas
├── docs/
│   ├── api-contracts/          # share these with frontend + AI members
│   └── postman/                # importable Postman collections
├── auth-service/               # DONE (Step 1)
├── api-gateway/                # DONE (Step 2)
├── resume-service/             # DONE (Step 5, AI member ka code + integration)
├── coding-service/             # DONE (Step 4)
├── aptitude-service/           # DONE (Step 3)
├── interview-service/          # DONE (Step 6, AI member ka code + integration)
├── analytics-service/          # DONE (Step 7, AI member ka RoadMapService + integration)
└── llm-gateway/                # AI member's service
(tools/sheet_to_json.py = problems sheet -> JSON)
```

## Ports
| Service | Port |
|---|---|
| api-gateway | 8080 |
| auth-service | 8081 |
| resume-service | 8082 |
| coding-service | 8083 |
| aptitude-service | 8084 |
| interview-service | 8085 |
| analytics-service | 8086 |
| llm-gateway | 8090 |
| aptitude-question-service | 8091 |

## Run auth-service
1. Requirements: JDK 17, Maven (ya IntelliJ ka built-in Maven), Docker (ya local MySQL 8).
2. MySQL start:
   ```
   docker compose up -d
   ```
   Docker nahi hai toh local MySQL use karo aur `auth-service/src/main/resources/application.yml` mein username/password badlo
   (ya env vars `DB_USERNAME`, `DB_PASSWORD` set karo).
3. IntelliJ mein **auth-service** folder ko "Open as Project" karo (pom.xml pe), phir `AuthServiceApplication` run karo.
   Ya terminal se:
   ```
   cd auth-service
   mvn spring-boot:run
   ```
4. Postman mein `docs/postman/interview-iq-auth.postman_collection.json` import karke test karo.

## Environment variables (optional, defaults dev ke liye hain)
| Variable | Default |
|---|---|
| DB_URL | jdbc:mysql://localhost:3306/auth_db?... |
| DB_USERNAME | root |
| DB_PASSWORD | root |
| JWT_SECRET | dev secret (min 32 chars) - production mein zaroor badlo |
| JWT_EXPIRATION_MS | 86400000 |
| CORS_ALLOWED_ORIGINS | http://localhost:5173,http://localhost:3000 |

## Admin user kaise banayein
Register API se sirf STUDENT bante hain. Admin ke liye register karke DB mein:
```sql
UPDATE auth_db.users SET role = 'ADMIN' WHERE email = 'your@email.com';
```
