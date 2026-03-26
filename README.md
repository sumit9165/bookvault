# 📚 BookVault

> A secure, full-stack digital library platform — Spring Boot 3.2 · MySQL 8 · MariaDB 12 · Redis 7 · Thymeleaf · Docker

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Quick Start — Development (MySQL)](#quick-start--development-mysql)
6. [Quick Start — Development (MariaDB)](#quick-start--development-mariadb)
7. [Quick Start — Production (Docker)](#quick-start--production-docker)
8. [Database Configuration — MySQL vs MariaDB](#database-configuration--mysql-vs-mariadb)
9. [MariaDB 12 — Complete Setup Guide](#mariadb-12--complete-setup-guide)
10. [Environment Variables Reference](#environment-variables-reference)
11. [Spring Profiles Reference](#spring-profiles-reference)
12. [Security Architecture](#security-architecture)
13. [API Reference](#api-reference)
14. [Default Credentials](#default-credentials)
15. [Serving in Production](#serving-in-production)
16. [Important Settings — Dev vs Prod](#important-settings--dev-vs-prod)
17. [Database Migrations (Flyway)](#database-migrations-flyway)
18. [Redis Caching Strategy](#redis-caching-strategy)
19. [Troubleshooting](#troubleshooting)

---

## Overview

BookVault is a production-ready book management and reading platform. Key capabilities:

- **Public access** — anyone sees book title, author, version release
- **User accounts** — registered users read full details and view PDFs in-browser
- **Admin panel** — admins create, edit, delete books with PDF upload and timestamps
- **Security-first** — CSRF, JWT, Redis sessions, DDoS rate limiting, XSS sanitization
- **Dual database** — runs on both **MySQL 8.x** and **MariaDB 10.x / 11.x / 12.x**

---

## Features

| Feature | Details |
|---------|---------|
| Authentication | JWT (HttpOnly cookie) + Refresh tokens in DB |
| Authorization | ADMIN / USER roles via Spring Security |
| Book CRUD | Create, update, soft-delete with auto timestamps |
| PDF Upload | 50 MB limit, streamed to browser |
| PDF Reader | In-browser viewer with zoom controls |
| Public API | Title / Author / Version visible without login |
| Search | Title, author, genre search |
| Genre Filtering | Dynamic tag-based filtering |
| Redis Cache | Books, genres, users cached with TTL |
| Rate Limiting | Bucket4j per-IP + Nginx layer |
| CSRF Protection | Cookie-based CSRF tokens |
| XSS Prevention | CSP headers + input sanitization |
| Soft Delete | Books never hard-deleted |
| Scheduled Jobs | Expired tokens auto-purged nightly |
| Dual Database | MySQL 8 and MariaDB 10/11/12 supported |
| Docker Ready | Full docker-compose with both DB options |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| Build | Gradle 8.6 |
| Database | MySQL 8.2 **or** MariaDB 11.4 / 12.x |
| Cache / Session | Redis 7.2 |
| ORM | Spring Data JPA + Hibernate 6 |
| Migrations | Flyway (MySQL + MariaDB compatible) |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| Templating | Thymeleaf 3 |
| Rate Limiting | Bucket4j |
| Reverse Proxy | Nginx 1.25 (production) |
| Containers | Docker + Docker Compose |

---

## Project Structure

```
bookvault/
├── src/main/java/com/bookvault/
│   ├── BookVaultApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java        # CSRF, CORS, CSP, rate limit filter chain
│   │   ├── RedisConfig.java           # 4 named caches with TTL
│   │   ├── DataInitializer.java       # Seeds admin on first boot
│   │   └── ScheduledTasksConfig.java  # Nightly token cleanup
│   ├── controller/
│   │   ├── AuthController.java        # /api/auth/**
│   │   ├── BookController.java        # /api/v1/books/**
│   │   └── PageController.java        # Thymeleaf page routes
│   ├── dto/                           # ApiResponse, AuthDto, BookDto
│   ├── entity/                        # User, Book (soft-delete), RefreshToken
│   ├── exception/                     # GlobalExceptionHandler + custom exceptions
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── RateLimitingFilter.java    # Bucket4j per-IP
│   ├── repository/                    # JPQL + soft-delete queries
│   ├── security/
│   │   └── UserDetailsServiceImpl.java
│   ├── service/
│   │   ├── AuthService.java / impl/AuthServiceImpl.java
│   │   ├── BookService.java  / impl/BookServiceImpl.java
│   │   └── FileStorageService.java
│   └── util/JwtUtil.java
│
├── src/main/resources/
│   ├── application.yml                # All profiles: dev, prod, mariadb, mariadb-prod
│   ├── db/migration/
│   │   ├── V1__initial_schema.sql     # MySQL + MariaDB compatible DDL
│   │   └── V2__seed_sample_books.sql  # Dev sample data
│   ├── static/css/main.css            # Dark luxury design system
│   ├── static/js/main.js              # Auth, API, toast, modals
│   └── templates/                     # Thymeleaf pages + admin panel
│
├── docker/
│   ├── mysql/init.sql                 # MySQL init
│   └── mariadb/init.sql               # MariaDB init
├── nginx/                             # nginx.conf + server block with rate limits
├── scripts/
│   ├── dev-start.sh
│   └── prod-deploy.sh
├── docker-compose.yml                 # mysql + mariadb + redis + app + nginx
├── Dockerfile                         # Multi-stage build
├── .env.example                       # Template — copy to .env
├── .gitignore
├── .dockerignore
└── build.gradle
```

---

## Quick Start — Development (MySQL)

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 21+ |
| Docker Desktop | 4.x+ |

### Step 1 — Set up environment

```bash
git clone https://github.com/sumit9165/bookvault.git
cd bookvault
cp .env.example .env
```

Open `.env` — for MySQL dev the defaults are fine, just change the passwords:

```dotenv
SPRING_PROFILE=dev
DB_HOST=localhost
DB_PORT=3306
```

### Step 2 — Start MySQL + Redis

```bash
docker compose up -d mysql redis
```

### Step 3 — Run the app

```bash
./gradlew bootRun
```

Open **http://localhost:8080**

---

## Quick Start — Development (MariaDB)

### Step 1 — Set the MariaDB profile in `.env`

```dotenv
SPRING_PROFILE=mariadb
DB_HOST=mariadb
DB_PORT=3306
DB_NAME=bookvault
DB_USERNAME=bookvault_user
DB_PASSWORD=yourpassword
```

### Step 2 — Start MariaDB + Redis via Docker

```bash
# The mariadb service requires the "mariadb" Docker Compose profile
docker compose --profile mariadb up -d mariadb redis
```

### Step 3 — Run the app

```bash
./gradlew bootRun
```

That's it — Flyway migrations run automatically against MariaDB.

---

## Quick Start — Production (Docker)

```bash
cp .env.example .env
# Edit .env — set SPRING_PROFILE, strong passwords, JWT_SECRET, ALLOWED_ORIGINS

# MySQL production
docker compose --profile production up -d --build

# MariaDB production
docker compose --profile mariadb --profile production up -d --build
```

---

## Database Configuration — MySQL vs MariaDB

BookVault supports both databases with **zero code changes** — you switch by changing `SPRING_PROFILE` and the JDBC URL in `.env`.

### How it works

| | MySQL | MariaDB |
|--|-------|---------|
| JDBC driver | `com.mysql.cj.jdbc.Driver` | `org.mariadb.jdbc.Driver` |
| JDBC URL prefix | `jdbc:mysql://` | `jdbc:mariadb://` |
| Hibernate dialect | `MySQLDialect` (auto) | `MariaDBDialect` (auto) |
| Flyway plugin | `flyway-mysql` | `flyway-mysql` (same — compatible) |
| Spring profile | `dev` / `prod` | `mariadb` / `mariadb-prod` |

### Quick comparison

| Feature | MySQL 8.x | MariaDB 10.x–12.x |
|---------|-----------|-------------------|
| FULLTEXT indexes | ✅ | ✅ |
| JSON columns | ✅ | ✅ (MariaDB 10.2+) |
| Window functions | ✅ | ✅ (MariaDB 10.2+) |
| ENUM type | ✅ | ✅ |
| utf8mb4 charset | ✅ | ✅ |
| InnoDB engine | ✅ | ✅ |
| ON UPDATE CURRENT_TIMESTAMP | ✅ | ✅ |
| Connection pool (HikariCP) | ✅ | ✅ |
| Spring Boot auto-detect | ✅ | ✅ (Hibernate 6+) |

---

## MariaDB 12 — Complete Setup Guide

### Option A — Docker (easiest)

```bash
# 1. Edit .env
SPRING_PROFILE=mariadb
DB_HOST=mariadb         # use Docker service name
DB_PORT=3306
DB_NAME=bookvault
DB_USERNAME=bookvault_user
DB_PASSWORD=your_strong_password
MARIADB_ROOT_PASSWORD=your_root_password

# 2. Start MariaDB + Redis
docker compose --profile mariadb up -d mariadb redis

# 3. Verify MariaDB is healthy
docker compose ps

# 4. Run the application
./gradlew bootRun
```

### Option B — Local MariaDB install (Ubuntu/Debian)

```bash
# Install MariaDB 11.x / 12.x
curl -LsS https://downloads.mariadb.com/MariaDB/mariadb_repo_setup | sudo bash -s -- --mariadb-server-version="mariadb-11.4"
sudo apt install mariadb-server -y

# Secure the installation
sudo mysql_secure_installation

# Create the database and user
sudo mariadb -u root -p << 'SQL'
CREATE DATABASE IF NOT EXISTS bookvault
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'bookvault_user'@'localhost'
  IDENTIFIED BY 'your_strong_password';

GRANT ALL PRIVILEGES ON bookvault.* TO 'bookvault_user'@'localhost';
FLUSH PRIVILEGES;
SQL
```

Then in `.env`:

```dotenv
SPRING_PROFILE=mariadb
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bookvault
DB_USERNAME=bookvault_user
DB_PASSWORD=your_strong_password
```

Run the app — Flyway migrates automatically.

### Option C — Local MariaDB install (macOS)

```bash
brew install mariadb
brew services start mariadb

# Create DB and user
mariadb -u root << 'SQL'
CREATE DATABASE IF NOT EXISTS bookvault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bookvault_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON bookvault.* TO 'bookvault_user'@'localhost';
FLUSH PRIVILEGES;
SQL
```

### Option D — Local MariaDB install (Windows)

1. Download MariaDB 11.x/12.x from https://mariadb.org/download/
2. Run the installer (keep default port 3306)
3. Open **HeidiSQL** or **MySQL Workbench** (both work with MariaDB)
4. Connect as root and run:

```sql
CREATE DATABASE IF NOT EXISTS bookvault
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'bookvault_user'@'localhost'
  IDENTIFIED BY 'your_strong_password';

GRANT ALL PRIVILEGES ON bookvault.* TO 'bookvault_user'@'localhost';
FLUSH PRIVILEGES;
```

5. In `.env`:

```dotenv
SPRING_PROFILE=mariadb
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bookvault
DB_USERNAME=bookvault_user
DB_PASSWORD=your_strong_password
```

### Option E — Remote/cloud MariaDB

If your MariaDB server is on a remote host (e.g., AWS RDS, DigitalOcean, PlanetScale):

```dotenv
SPRING_PROFILE=mariadb-prod
DB_HOST=your-mariadb-host.example.com
DB_PORT=3306
DB_NAME=bookvault
DB_USERNAME=bookvault_user
DB_PASSWORD=your_strong_password
DB_SSL=true
```

For SSL with RDS MariaDB, also add to your `application.yml` datasource URL:
```
&sslMode=REQUIRED&trustServerCertificate=false
```

### Verifying MariaDB is connected

After starting the app, check logs for:

```
INFO  DataInitializer     : Default admin user created: admin
INFO  FlywayAutoConfiguration : Flyway Community Edition ... has successfully applied 2 migrations
```

If you see `Communications link failure`, MariaDB isn't running or `DB_HOST` is wrong.

### MariaDB version compatibility matrix

| MariaDB Version | Supported | Notes |
|----------------|-----------|-------|
| 10.5 | ✅ | Minimum recommended |
| 10.6 LTS | ✅ | Long-term support |
| 10.11 LTS | ✅ | Latest 10.x LTS |
| 11.4 LTS | ✅ | Tested, recommended |
| 12.0 | ✅ | Latest; fully compatible |

---

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILE` | `dev` | `dev` · `prod` · `mariadb` · `mariadb-prod` |
| `SERVER_PORT` | `8080` | App HTTP port |
| `DB_HOST` | `localhost` | Database hostname |
| `DB_PORT` | `3306` | Database port |
| `DB_NAME` | `bookvault` | Schema name |
| `DB_USERNAME` | `bookvault_user` | DB user |
| `DB_PASSWORD` | — | **Change this!** |
| `DB_SSL` | `false` | Enable SSL for DB connection |
| `MYSQL_ROOT_PASSWORD` | `rootpass` | MySQL Docker root password |
| `MARIADB_ROOT_PASSWORD` | `rootpass` | MariaDB Docker root password |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | — | Redis auth (set in prod) |
| `JWT_SECRET` | — | **≥64 random chars — MUST change!** |
| `JWT_EXPIRATION` | `86400000` | Access token TTL ms (24h) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL ms (7d) |
| `ADMIN_USERNAME` | `admin` | Bootstrap admin username |
| `ADMIN_EMAIL` | `admin@bookvault.com` | Bootstrap admin email |
| `ADMIN_PASSWORD` | — | **Change this!** |
| `FILE_UPLOAD_DIR` | `./uploads` | PDF storage directory |
| `MAX_FILE_SIZE` | `50MB` | Max PDF upload |
| `ALLOWED_ORIGINS` | `http://localhost:8080` | CORS allowed origins |
| `RATE_LIMIT_CAPACITY` | `100` | Bucket4j max tokens |
| `RATE_LIMIT_REFILL` | `100` | Tokens refilled per period |
| `RATE_LIMIT_PERIOD` | `60` | Refill period in seconds |
| `LOG_LEVEL` | `DEBUG` | App log level |
| `SHOW_SQL` | `true` | Print Hibernate SQL |
| `THYMELEAF_CACHE` | `false` | Template caching |
| `SSL_ENABLED` | `false` | Enable Spring Boot SSL |
| `SSL_KEYSTORE` | — | Path to keystore file |
| `SSL_KEYSTORE_PASSWORD` | — | Keystore password |

---

## Spring Profiles Reference

| Profile | Database | Use Case | Key Settings |
|---------|----------|----------|-------------|
| `dev` | MySQL 8.x | Local development | SQL logging on, no template cache, DEBUG logs |
| `prod` | MySQL 8.x | Production | Pool=20, template cache on, WARN logs, static cache 1d |
| `mariadb` | MariaDB any | Local development with MariaDB | MariaDB driver, SQL logging on, DEBUG logs |
| `mariadb-prod` | MariaDB any | Production with MariaDB | MariaDB driver, SSL=true, pool=20, WARN logs |

### Activating profiles

**Via `.env`:**
```dotenv
SPRING_PROFILE=mariadb
```

**Via command line:**
```bash
./gradlew bootRun --args='--spring.profiles.active=mariadb'
```

**Via Java system property:**
```bash
java -Dspring.profiles.active=mariadb-prod -jar bookvault.jar
```

**Via Docker Compose environment:**
```yaml
environment:
  SPRING_PROFILE: mariadb-prod
```

---

## Security Architecture

### Authentication Flow

```
Browser                 BookVault App              Redis / MySQL/MariaDB
  │                          │                           │
  │─ POST /api/auth/login ──>│                           │
  │                          │─ Validate credentials ───>│
  │                          │─ Generate JWT ────────────│
  │                          │─ Store RefreshToken ──────>│
  │<─ Set-Cookie: jwt_token ─│                           │
  │                          │                           │
  │─ GET /api/v1/books/.. ──>│                           │
  │  (Cookie: jwt_token)     │─ JwtAuthFilter validates  │
  │                          │─ Load user from Redis ────>│
  │<─ 200 OK ────────────────│                           │
```

### Security Layers

| # | Layer | Mechanism | Protects Against |
|---|-------|-----------|-----------------|
| 1 | Nginx | 30 req/s global, 5/min on auth routes | DDoS |
| 2 | Bucket4j | 100 req/60s per IP in-app | DDoS bypass |
| 3 | Spring Security | CSRF tokens (CookieCsrfTokenRepository) | CSRF |
| 4 | Headers | Content-Security-Policy, X-XSS-Protection | XSS |
| 5 | Service layer | Input sanitization in BookServiceImpl | Stored XSS |
| 6 | JwtAuthFilter | HttpOnly cookie + Bearer header support | Token theft |
| 7 | BCrypt-12 | Password hashing | Password exposure |
| 8 | FileStorageService | Path traversal guard | LFI |
| 9 | @PreAuthorize | Method-level role checks | Privilege escalation |
| 10 | Soft delete | No hard deletes | Accidental data loss |
| 11 | Spring Session | Redis-backed sessions | Session fixation |

---

## API Reference

### Auth (no auth required)

| Method | Path | Body | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | `{ usernameOrEmail, password }` | Login |
| POST | `/api/auth/register` | `{ username, email, password }` | Register |
| POST | `/api/auth/refresh` | `{ refreshToken }` | New access token |
| POST | `/api/auth/logout` | — | Revoke + clear cookie |
| GET | `/api/auth/me` | — | Current user (auth required) |

### Public Books (no auth)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/books/public` | Paginated list (title, author, version) |
| GET | `/api/v1/books/public/search?query=X` | Search |
| GET | `/api/v1/books/public/{id}` | Single book |
| GET | `/api/v1/books/genres` | All genres |
| GET | `/api/v1/books/public/genre/{genre}` | Books by genre |

### User (JWT required)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/books/{id}/read` | Full book details |
| GET | `/api/v1/books/{id}/pdf` | Stream PDF |

### Admin (ADMIN role required)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/books` | Create (multipart: book JSON + pdf file) |
| PUT | `/api/v1/books/{id}` | Update |
| DELETE | `/api/v1/books/{id}` | Soft-delete |
| GET | `/api/v1/books/admin/all` | All books including private |

---

## Default Credentials

Seeded on first startup from `.env`:

| Field | Default |
|-------|---------|
| Username | `admin` |
| Email | `admin@bookvault.com` |
| Password | `Admin@SecurePassword123!` |
| Role | `ADMIN` |

⚠️ **Change `ADMIN_PASSWORD` before any production deployment.**

---

## Serving in Production

### Option A — Docker Compose with MySQL (recommended)

```bash
# .env: SPRING_PROFILE=prod
docker compose --profile production up -d --build
```

### Option B — Docker Compose with MariaDB

```bash
# .env: SPRING_PROFILE=mariadb-prod, DB_HOST=mariadb
docker compose --profile mariadb --profile production up -d --build
```

### Option C — JAR + external MySQL

```bash
./gradlew bootJar
java -jar build/libs/bookvault.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://host:3306/bookvault?useSSL=true" \
  --spring.datasource.username=bookvault_user \
  --spring.datasource.password=password \
  --app.jwt.secret=your_64_char_secret
```

### Option D — JAR + external MariaDB

```bash
./gradlew bootJar
java -jar build/libs/bookvault.jar \
  --spring.profiles.active=mariadb-prod \
  --spring.datasource.url="jdbc:mariadb://host:3306/bookvault?useSSL=true" \
  --spring.datasource.username=bookvault_user \
  --spring.datasource.password=password \
  --app.jwt.secret=your_64_char_secret
```

### Enable SSL (Nginx approach — recommended)

Place `fullchain.pem` + `privkey.pem` in `nginx/ssl/`, then uncomment the HTTPS server block in `nginx/conf.d/bookvault.conf` and restart nginx.

### Persistent Volumes

| Volume | Purpose |
|--------|---------|
| `mysql_data` | MySQL data files |
| `mariadb_data` | MariaDB data files |
| `redis_data` | Redis AOF persistence |
| `uploads_data` | Uploaded PDFs |
| `nginx_logs` | Access + error logs |

**Backup:**
```bash
# DB dump — MySQL
docker compose exec mysql mysqldump -u root -p bookvault > backup-$(date +%F).sql

# DB dump — MariaDB
docker compose exec mariadb mariadb-dump -u root -p bookvault > backup-$(date +%F).sql

# Uploads
docker run --rm -v bookvault_uploads_data:/data -v $(pwd):/out \
  alpine tar czf /out/uploads-$(date +%F).tar.gz -C /data .
```

---

## Important Settings — Dev vs Prod

### Development

| Setting | Value | Why |
|---------|-------|-----|
| `SHOW_SQL` | `true` | See every query |
| `LOG_LEVEL` | `DEBUG` | Verbose output |
| `THYMELEAF_CACHE` | `false` | Hot-reload templates |
| `DB_SSL` | `false` | Simpler local config |
| Profile | `dev` or `mariadb` | |

### Production

| Setting | Recommended | Why |
|---------|-------------|-----|
| `SHOW_SQL` | `false` | Never leak SQL |
| `LOG_LEVEL` | `WARN` | Reduce I/O |
| `THYMELEAF_CACHE` | `true` | Faster responses |
| `DB_SSL` | `true` | Encrypt DB traffic |
| `REDIS_PASSWORD` | strong | Secure cache |
| `JWT_SECRET` | 64+ random chars | Prevent token forgery |
| `ALLOWED_ORIGINS` | `https://yourdomain.com` | Strict CORS |
| Profile | `prod` or `mariadb-prod` | |
| HikariCP pool | 20 (auto-set) | More concurrency |

---

## Database Migrations (Flyway)

Flyway runs **automatically on startup** for both MySQL and MariaDB.

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__initial_schema.sql` | Schema — compatible with MySQL 8 + MariaDB 10–12 |
| V2 | `V2__seed_sample_books.sql` | Sample dev data |

**All DDL in V1 is compatible with both databases:**
- `ENUM`, `BOOLEAN`, `FULLTEXT INDEX`, `InnoDB` — all work identically
- `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` — supported on both
- `utf8mb4 / utf8mb4_unicode_ci` — supported on both

**Adding a migration:**
1. Create `src/main/resources/db/migration/V3__your_change.sql`
2. Restart the app — it applies automatically
3. Never modify an already-applied migration file

**Repair Flyway (dev only):**
```bash
# Only if checksums mismatch after editing a migration (dev only!)
./gradlew flywayRepair
```

---

## Redis Caching Strategy

| Cache | TTL | Evicted When |
|-------|-----|-------------|
| `publicBooks` | 10 min | Book create / update / delete |
| `books` | 5 min | Book update / delete |
| `genres` | 30 min | Book create / update / delete |
| `users` | 15 min | User update |

Spring Session uses Redis with 1-hour TTL.

**Flush all caches:**
```bash
docker compose exec redis redis-cli FLUSHDB
```

---

## Troubleshooting

### MySQL: "Communications link failure"

```bash
docker compose logs mysql        # check for errors
docker compose ps                # wait for "healthy" status
docker compose up -d mysql       # restart if needed
```

### MariaDB: "Communications link failure"

```bash
docker compose --profile mariadb logs mariadb
docker compose --profile mariadb ps
# Ensure SPRING_PROFILE=mariadb in .env
# Ensure DB_HOST=mariadb (matches the Docker service name)
```

### MariaDB: "Unknown database 'bookvault'"

```bash
docker compose --profile mariadb exec mariadb \
  mariadb -u root -p -e "CREATE DATABASE IF NOT EXISTS bookvault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### "Access denied for user 'bookvault_user'@'%'"

The user was not created correctly. Recreate:

```bash
# MySQL
docker compose exec mysql mysql -u root -p -e \
  "GRANT ALL PRIVILEGES ON bookvault.* TO 'bookvault_user'@'%'; FLUSH PRIVILEGES;"

# MariaDB
docker compose --profile mariadb exec mariadb mariadb -u root -p -e \
  "GRANT ALL PRIVILEGES ON bookvault.* TO 'bookvault_user'@'%'; FLUSH PRIVILEGES;"
```

### Flyway checksum mismatch

Never edit an already-applied migration. If you must fix a dev-only mistake:

```bash
# Drop the DB schema and let Flyway recreate it (dev only!)
docker compose exec mysql mysql -u root -p -e \
  "DROP DATABASE bookvault; CREATE DATABASE bookvault CHARACTER SET utf8mb4;"
# Then restart the app — all migrations will re-run
```

### MariaDB: Hibernate dialect warning

If you see a warning about dialect detection, ensure `SPRING_PROFILE=mariadb` (or `mariadb-prod`) is set. The `mariadb` profile explicitly sets `org.hibernate.dialect.MariaDBDialect`.

### JWT cookie not sent after login

In production with HTTPS, set `cookie.setSecure(true)` in `AuthController.setJwtCookie()`. Already handled via the `prod`/`mariadb-prod` profile — enable `SSL_ENABLED=true` or use Nginx SSL termination.

### Rate limiting too aggressive in dev

```dotenv
RATE_LIMIT_CAPACITY=1000
RATE_LIMIT_REFILL=1000
RATE_LIMIT_PERIOD=60
```

### Out of memory in Docker

Increase Docker Desktop memory to ≥3 GB. The JVM is configured with `-XX:MaxRAMPercentage=75.0` to respect the container limit.

### Check which database is active at runtime

```bash
# Confirm which driver/dialect Spring Boot detected
docker compose logs app | grep -i "dialect\|HikariPool\|driver"
```

---

## License

MIT License — free for personal and commercial use.

---

Made with ❤️ — Spring Boot 3.2 · Java 21 · MySQL 8 / MariaDB 12 · Redis 7 · Thymeleaf · Bucket4j · Docker
