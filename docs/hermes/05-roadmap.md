# Roadmap

Option A: one 3-hour session per week, starting Monday 2026-10-19. From 2026-10-25 the sessions are fixed on Sundays 13:00–16:00 (calendar series until 2027-01-17). Gantt chart: [`../diagrams/gantt.puml`](../diagrams/gantt.puml).

Status 2026-10-23: M1–M7 are done, well ahead of plan (M2–M7 merged on 2026-10-17/18 through PRs #1–#4). What remains is content, the manual checks and M8. Project content is collected in Notion (`P008_portfolio` → `P008_Showcase_Projekte`). Test results: [`06-test-results.md`](06-test-results.md).

| Planned week | Milestone | Done when | Status |
| :--- | :--- | :--- | :--- |
| 2026-10-19 | M1 Environment and skeleton | `mvnw verify` passes locally and in CI; app serves a placeholder page | Done 2026-10-16 |
| 2026-10-26 | M2 Data model and seed data | Integration tests pass against real PostgreSQL | Done 2026-10-17 (PR #1): V1 schema, V2 seed, JPA entities, ITs |
| 2026-11-02 | M3 Layout, Bootstrap, i18n | Home page browsable in EN and DE with switcher | Done 2026-10-17: /en and /de routing, shared layout, Bootstrap 5.3.8 WebJar, 404/5xx pages |
| 2026-11-09 | Content preparation | Texts, screenshots (WebP), alt texts, certification details ready | Partly: 26 certifications seeded from the PDFs (V3); project texts are still placeholders and there are no screenshots |
| 2026-11-16 | M4 Project pages | Three projects viewable with real content in EN and DE | Pages done 2026-10-17; real content still open (new migration needed) |
| 2026-11-23 | M5 Certifications and legal pages | Pages render in both languages | Done 2026-10-17 (PR #2); privacy notice dated 17 October 2026, legal texts still to review against official guidance |
| 2026-11-30, 12-07 | M6 Contact form | Test mail arrives (Mailpit locally); failure paths tested | Done 2026-10-17 (PR #3): honeypot, rate limit 5/10 min, mail via SMTP, one-year retention |
| 2026-12-14, 12-21 | M7 Quality pass | Automated checks green; manual checklist done | Automated part done 2026-10-17 (PR #4): Playwright, axe-core 0 violations, Lighthouse 100; manual checklist open |
| 2026-12-28, 2027-01-04 | M8 Production | Live over HTTPS; smoke test passes | App live since 2026-10-23 (image built on the server); mail password, CD, backups and monitoring open |
| 2027-01-11 | Buffer | Ends 2027-01-17 | |

## Remaining before go-live

- [ ] Real project texts (EN + DE), screenshots (WebP) and alt texts → new Flyway migration (never edit V2)
- [ ] Credential links for certifications where public ones exist
- [ ] Manual accessibility checks in [`06-test-results.md`](06-test-results.md) (keyboard, NVDA, 200% zoom)
- [ ] Impressum and privacy notice checked against official guidance; date updated
- [ ] Friends proofread EN and DE
- [ ] M8: deploy the app (checklist below)

## M1 checklist (native Windows)

- [x] Folder layout from Phase 5, package `ch.gurung.rahul.portfolio`
- [x] Project generated with Spring Initializr (Boot 4.1.1, Java 25), Maven wrapper included
- [x] `.gitattributes` (LF) and `.editorconfig`
- [x] Local PostgreSQL + Mailpit via `deploy/docker-compose.dev.yml`
- [x] HERMES documents and first ADRs in `docs/`
- [x] GitHub Actions CI workflow (build, test, secret scan)
- [x] README with setup instructions
- [x] Install JDK 25 (Temurin) and Docker Desktop
- [x] Create the GitHub repository and push
- [x] `mvnw verify` green locally and in CI
- [x] Infomaniak mailbox `rahul@gurung.ch` created, SMTP send tested
- [ ] Infomaniak: two-factor authentication and auto-renew on; check the domain term (shows expiry 2027-10-15, 2 years were ordered)

## M8 head start (done 2026-10-23)

Server details: [ADR 0004](../adr/0004-hosting-infomaniak.md#server-ordered-2026-10-23).

- [x] VPS ordered (Infomaniak VPS Lite, 2 vCPU / 4 GB)
- [x] Key-only SSH, ufw, automatic security updates, timezone
- [x] Docker Engine + Compose, log rotation
- [x] Infomaniak firewall: TCP 80 and 443 open
- [x] DNS A + AAAA for `www.portfolio.rahul.gurung.ch` and `portfolio.rahul.gurung.ch`
- [x] HTTPS placeholder page live (Caddy), redirect checked
- [x] App deployed 2026-10-23 from commit 945a799: code copied to `~/portfolio`, image `portfolio:local` built on the server, `deploy/` stack (Caddy, app, PostgreSQL) running; placeholder stopped
- [x] `deploy/.env` on the server (chmod 600, generated DB password)
- [x] Smoke test from outside: all pages 200, unknown page 404, `/actuator` 404, both redirects, security headers, 26 certificates
- [ ] `MAIL_PASSWORD` in `deploy/.env` (set by Rahul), then `docker compose up -d app`, then a test message
- [ ] Non-root deploy user and deploy key; switch `deploy.yml` from manual to GHCR images
- [ ] Nightly backup (`backup.sh` in cron), off-site copy, restore test
- [ ] Uptime monitor
- [ ] Enable HSTS in the Caddyfile
- [ ] Delete `~/placeholder` on the server

Copy for school: the project is also on git.gibb.ch (`m293-inf2025n/rahul_gurung`, branch `p008-portfolio`); `main` there holds the M293 work.

## Redeploy by hand (until CD is set up)

On your PC: `git archive --format=tar.gz -o portfolio.tar.gz HEAD`, copy it with `scp` to the server, then on the server unpack it into `~/portfolio` (keep `deploy/.env`), `docker build -t portfolio:local .` and `cd deploy && docker compose up -d app`.

## Open items

- Verify that SBB backend teams use Spring Boot (job ads).
- Verify HERMES phase and document names at hermes.admin.ch.
