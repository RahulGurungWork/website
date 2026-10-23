# P008-Portfolio

Personal portfolio website of Rahul Gurung: Spring Boot 4.1 (Java 25), Thymeleaf, PostgreSQL, Flyway, Bootstrap.
Live at <https://www.portfolio.rahul.gurung.ch> since 2026-10-23.

Release 1: three showcase projects, skills and about sections, certifications, CV as PDF, contact form, Impressum and privacy notice, in English and German.
Plan, requirements and decisions: [`docs/`](docs/). Status and what is still open: [`docs/hermes/05-roadmap.md`](docs/hermes/05-roadmap.md); deployment: [`docs/hermes/07-go-live.md`](docs/hermes/07-go-live.md).

## Prerequisites (native Windows)

| Tool | Install |
| :--- | :--- |
| JDK 25 (Temurin) | `winget install EclipseAdoptium.Temurin.25.JDK` |
| Docker Desktop | `winget install Docker.DockerDesktop` (needs WSL2; restart afterwards) |
| Git | already installed |
| IDE | IntelliJ IDEA Community or VS Code with Java extensions |

Maven is not needed: the project ships the Maven wrapper (`mvnw.cmd`).
Check package names with `winget search` if an install fails.

## Run locally

```powershell
# 1. Start PostgreSQL and Mailpit (Docker Desktop must be running)
docker compose -f deploy/docker-compose.dev.yml up -d

# 2. Start the app (profile "dev" is the default)
.\mvnw.cmd spring-boot:run
```

Open <http://localhost:8080>. Outgoing mail is caught by Mailpit at <http://localhost:8025>.

Alternative without the compose file: run `TestPortfolioApplication` from `src/test` in the IDE; it starts a throwaway PostgreSQL through Testcontainers.

## Test

```powershell
.\mvnw.cmd verify
```

Integration tests use Testcontainers, so Docker Desktop must be running.

## Project layout

```text
.github/workflows/   CI (build, test incl. browser tests, secret scan, Lighthouse) and deploy (manual trigger)
deploy/              docker-compose.yml (production), docker-compose.dev.yml (local), Caddyfile, backup.sh,
                     smoke-test.sh, .env.example
docs/hermes/         Charter, requirements, risks, test concept, roadmap, test results, go-live checklist
docs/adr/            Architecture decision records
docs/design/         Home page mockup
docs/diagrams/       PlantUML sources
e2e/                 Where the browser tests live (README)
src/main/java/ch/gurung/rahul/portfolio/
    config/ common/ home/ project/ technology/ certification/ contact/ legal/ cv/
src/main/resources/
    application*.yml  db/migration/  i18n/  templates/  static/
```

## Conventions

- Layered, package-by-feature: Controller → Service → Repository.
- Classes `ProjectController`, `ProjectService`; tests `*Test` (unit/feature) and `*IT` (integration).
- Tables and columns in `snake_case`; templates and URLs in lowercase kebab-case (`/en/projects/lerndoku-generator`).
- Flyway: `V<n>__<description>.sql`. Never edit an applied migration; add a new one.
- Git: `main` plus short-lived `feature/...` branches; Conventional Commits (`feat:`, `fix:`, `docs:`).
- Keep file names lowercase: Windows ignores case, the Linux server does not.
- No secrets in Git. Production values go in `deploy/.env` on the server.
