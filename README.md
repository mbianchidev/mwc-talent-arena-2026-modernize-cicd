# TelcoRec — Telco Invoice Reconciliation System

A legacy **Java EJB 3.x** monorepo for reconciling telco subscriber invoices against
CDR (Call Detail Record) consumption data.  Built for **TelcoCorp Italia S.r.l.**

> **Local-only application** — designed to run on `localhost` with no external
> infrastructure dependencies.

---

## Repository layout

```
.
├── backend/                  Java EJB WAR — Apache TomEE Plus
│   ├── pom.xml
│   └── src/main/java/com/telco/reconciliator/
│       ├── ejb/              Stateless session beans
│       ├── model/            JPA entities
│       ├── rest/             JAX-RS REST resources (/api/*)
│       └── startup/          @Singleton @Startup data seeder
├── frontend/                 Vanilla HTML / Bootstrap 5 SPA
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js
├── database/
│   ├── schema.sql            H2-compatible DDL reference
│   └── seed.sql              DML reference (same data as Java seeder)
├── scripts/
│   ├── deploy.sh             One-shot local deployment orchestrator
│   └── stop-all.sh           Teardown script
└── pom.xml                   Parent POM (monorepo root)
```

---

## Prerequisites

| Tool   | Minimum version |
|--------|----------------|
| Java   | 11             |
| Maven  | 3.6            |

> Maven downloads TomEE Plus 8.0.14 automatically on first run (~60 MB).

---

## Quick start

```bash
# Clone and enter the repo
cd mwc-talent-arena-2026-modernize-cicd

# One-command deploy (builds WAR + starts TomEE + serves frontend)
./scripts/deploy.sh

# Or, start only the backend manually:
cd backend
mvn clean package -DskipTests
mvn tomee:run
```

Once TomEE is up:

| URL | Description |
|-----|-------------|
| http://localhost:8080/reconciliator/api/customers | REST — list customers |
| http://localhost:8080/reconciliator/api/invoices  | REST — list invoices  |
| http://localhost:8080/reconciliator/api/reconciliation/stats | Reconciliation stats |
| frontend/index.html | Open directly in browser |

### Run reconciliation via the UI

1. Open `frontend/index.html` in your browser
2. Navigate to the **Riconciliazione** tab
3. Click **"Avvia Riconciliazione Completa"** — the engine compares each invoice
   against CDR records and flags MATCHED / OVERCHARGED / UNDERCHARGED / MISSING

### Run reconciliation via REST

```bash
# Reconcile all unprocessed invoices
curl -X POST http://localhost:8080/reconciliator/api/reconciliation/run/all

# Reconcile a single invoice (id = 5)
curl -X POST http://localhost:8080/reconciliator/api/reconciliation/run/5

# Get stats
curl http://localhost:8080/reconciliator/api/reconciliation/stats
```

---

## Deploy script options

```
Usage: ./scripts/deploy.sh [OPTIONS]

  --skip-build      Skip mvn clean package (use existing WAR)
  --skip-frontend   Do not start the frontend HTTP server
  --port=PORT       TomEE HTTP port (default: 8080)
  --help            Show this message
```

Logs are written to `~/.telcorec/logs/deploy-YYYYMMDD.log`.

To stop everything:

```bash
./scripts/stop-all.sh
```

---

## Mock data

The `DataLoaderBean` (`@Singleton @Startup`) seeds the embedded H2 database on
first startup with realistic Italian telco data:

- **4 service plans**: Mobile Base 5 GB · Mobile Plus 20 GB · Fibra 500 M + Mobile · Fibra Ultra 1 Gbps
- **8 subscribers** across Milano, Roma, Napoli, Torino, Bologna and Firenze
- **24 invoices** (Oct – Dec 2025, three billing cycles per subscriber)
- **CDR consumption records** with intentional discrepancies so the reconciliation
  engine has something to find

---

## CORS

The backend includes a JAX-RS `CorsFilter` that adds `Access-Control-Allow-Origin: *`
to all responses. This allows the frontend to be opened from a `file://` URL or served
on a different port without browser CORS errors.

---

## Internationalisation (i18n)

The frontend supports three languages, switchable from the navbar:

| Code | Language |
|------|----------|
| `en` | English  |
| `it` | Italian (default) |
| `ca` | Catalan  |

The chosen language is persisted in `localStorage` (`telcorec-lang` key) and
restored on page reload. All static labels, error messages, and toasts are
translated.

---

## Architecture

```
  Browser (SPA)
      │  fetch /api/*
      ▼
  Apache TomEE Plus 8.0.14
  ┌────────────────────────────────────┐
  │  JAX-RS  (CXF)                     │
  │   └─► CustomerResource             │
  │   └─► InvoiceResource              │
  │   └─► ReconciliationResource       │
  │                                    │
  │  EJB Container (OpenEJB)           │
  │   └─► CustomerServiceBean          │
  │   └─► InvoiceServiceBean           │
  │   └─► ReconciliationServiceBean    │
  │   └─► DataLoaderBean (startup)     │
  │                                    │
  │  JPA / OpenJPA                     │
  │   └─► H2 file DB (~/.telcorec/db/) │
  └────────────────────────────────────┘
```

---

## Docker

The application can be run in containers using Docker Compose.  Only Docker
(with Compose v2) is required — no local Java, Maven or nginx installation.

### Services

| Service    | Image base            | Container port | Host port | Description |
|------------|-----------------------|:--------------:|:---------:|-------------|
| `backend`  | eclipse-temurin:11    | 8080           | 8080      | TomEE Plus 8.0.16 — Java EJB backend |
| `frontend` | nginx:alpine          | 3000           | 3000      | Static SPA + reverse proxy to backend |

### Quick start with Docker

```bash
# Build images and start both services
docker compose up --build

# Or run in detached mode
docker compose up --build -d
```

Once healthy the frontend is at **http://localhost:3000** and the REST API is
available at both:

- **http://localhost:3000/reconciliator/api/** (via nginx proxy)
- **http://localhost:8080/reconciliator/api/** (direct backend access)

### Run tests inside Docker

```bash
# Run the backend test suite in a disposable container
docker compose run --rm --no-deps backend bash -c \
  "cd /build && mvn test -B"

# Or build the test stage locally (no Compose needed)
docker run --rm -v "$(pwd)":/src -w /src/backend \
  maven:3.9-eclipse-temurin-11 mvn test -B
```

### Stop & clean up

```bash
# Stop containers
docker compose down

# Stop containers and remove persistent H2 data volume
docker compose down -v
```

### Environment variables

| Variable         | Default | Description |
|------------------|---------|-------------|
| `BACKEND_PORT`   | `8080`  | Host port mapped to backend |
| `FRONTEND_PORT`  | `3000`  | Host port mapped to frontend |

Override via `.env` file or inline: `FRONTEND_PORT=8080 docker compose up`.

---

## Modernisation target

This application is intentionally written in a **legacy style** (EJB 3.x, manual
JNDI datasource, bash deployment) to serve as the starting point for a CI/CD
modernisation exercise at **MWC Talent Arena 2026**.
