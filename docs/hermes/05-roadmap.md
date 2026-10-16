# Roadmap

Option A: one 3-hour session per week, starting Monday 2026-10-19. Gantt chart: [`../diagrams/gantt.puml`](../diagrams/gantt.puml).

| Week of | Milestone | Done when |
| :--- | :--- | :--- |
| 2026-10-19 | M1 Environment and skeleton | `mvnw verify` passes locally and in CI; app serves a placeholder page |
| 2026-10-26 | M2 Data model and seed data | Integration tests pass against real PostgreSQL |
| 2026-11-02 | M3 Layout, Bootstrap, i18n | Home page browsable in EN and DE with switcher |
| 2026-11-09 | Content preparation | Texts, screenshots (WebP), alt texts, certification details ready |
| 2026-11-16 | M4 Project pages | Three projects viewable with real content in EN and DE |
| 2026-11-23 | M5 Certifications and legal pages | Pages render in both languages |
| 2026-11-30, 12-07 | M6 Contact form | Test mail arrives (Mailpit locally); failure paths tested |
| 2026-12-14, 12-21 | M7 Quality pass | Automated checks green; manual checklist done |
| 2026-12-28, 2027-01-04 | M8 Production | Live over HTTPS; smoke test passes |
| 2027-01-11 | Buffer | Ends 2027-01-17 |

## M1 checklist (native Windows)

- [x] Folder layout from Phase 5, package `ch.gurung.rahul.portfolio`
- [x] Project generated with Spring Initializr (Boot 4.1.1, Java 25), Maven wrapper included
- [x] `.gitattributes` (LF) and `.editorconfig`
- [x] Local PostgreSQL + Mailpit via `deploy/docker-compose.dev.yml`
- [x] HERMES documents and first ADRs in `docs/`
- [x] GitHub Actions CI workflow (build, test, secret scan)
- [x] README with setup instructions
- [ ] Install JDK 25 (Temurin) and Docker Desktop
- [ ] Create the GitHub repository and push
- [ ] `mvnw verify` green locally and in CI
- [ ] Infomaniak: two-factor authentication and auto-renew on; check the kSuite mailbox

## Open items

- Verify that SBB backend teams use Spring Boot (job ads).
- Verify HERMES phase and document names at hermes.admin.ch.
- Confirm live Infomaniak VPS Lite price before ordering (M7/M8).
- Confirm the free mailbox can send via SMTP.
