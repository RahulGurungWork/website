# Roadmap

Option A: one 3-hour session per week, starting Monday 2026-10-19. From 2026-10-25 the sessions are fixed on Sundays 13:00–16:00 (calendar series until 2027-01-17). Gantt chart (original plan): [`../diagrams/gantt.puml`](../diagrams/gantt.puml).

Status 2026-10-23: M1–M7 are done and the site is live at <https://www.portfolio.rahul.gurung.ch> (commit `355371c`), well ahead of the plan. Open: the M8 follow-ups in [`07-go-live.md`](07-go-live.md) and the manual checks in [`06-test-results.md`](06-test-results.md).

| Planned week | Milestone | Done when | Status |
| :--- | :--- | :--- | :--- |
| 2026-10-19 | M1 Environment and skeleton | `mvnw verify` passes locally and in CI; app serves a placeholder page | Done 2026-10-16 |
| 2026-10-26 | M2 Data model and seed data | Integration tests pass against real PostgreSQL | Done 2026-10-17 (PR #1): V1 schema, V2 seed, JPA entities, ITs |
| 2026-11-02 | M3 Layout, Bootstrap, i18n | Home page browsable in EN and DE with switcher | Done 2026-10-17: /en and /de routing, shared layout, Bootstrap 5.3.8 WebJar, 404/5xx pages |
| 2026-11-09 | Content preparation | Texts, screenshots (WebP), alt texts, certification details ready | Done 2026-10-18: real project texts (V4), 26 certifications (V3) grouped by category (V5); drawn illustrations instead of screenshots; texts accepted by Rahul 2026-10-23 |
| 2026-11-16 | M4 Project pages | Three projects viewable with real content in EN and DE | Done 2026-10-17/18: list and detail pages, illustrations, real content |
| 2026-11-23 | M5 Certifications and legal pages | Pages render in both languages | Done 2026-10-17 (PR #2); legal texts still to review against official guidance |
| 2026-11-30, 12-07 | M6 Contact form | Test mail arrives (Mailpit locally); failure paths tested | Done 2026-10-17 (PR #3): honeypot, rate limit 5/10 min, mail via SMTP, one-year retention |
| 2026-12-14, 12-21 | M7 Quality pass | Automated checks green; manual checklist done | Automated part done 2026-10-17 (PR #4), re-run 2026-10-23: 129 tests pass; manual checklist open |
| 2026-12-28, 2027-01-04 | M8 Production | Live over HTTPS; smoke test passes | Live since 2026-10-23, deployed by hand; mail password, CD, backups, monitoring, HSTS open ([`07-go-live.md`](07-go-live.md)) |
| 2027-01-11 | Buffer | Ends 2027-01-17 | |

Added beyond the plan on 2026-10-18 and live since 2026-10-23: Skills and About sections on the home page, and the CV as PDF (FR10, originally "later").

## Remaining

- [ ] M8 follow-ups: see [`07-go-live.md`](07-go-live.md) (mail password and test message first)
- [ ] Manual accessibility checks in [`06-test-results.md`](06-test-results.md) (keyboard, NVDA, 200% zoom)
- [ ] Impressum and privacy notice checked against official guidance; date updated
- [ ] Friends proofread EN and DE
- [ ] Credential links for certifications where public ones exist
- [ ] Real screenshots to replace the illustrations (optional)

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
- [ ] Infomaniak: two-factor authentication and auto-renew on; domain term (moved to [`07-go-live.md`](07-go-live.md), block 1)

## Repositories

- GitHub `RahulGurungWork/website`, branch `main`: the source of truth; feature branches are merged here
- git.gibb.ch `m293-inf2025n/rahul_gurung`, branch `p008-portfolio`: copy for school, pushed from `main`; `main` there holds the M293 work and is never pushed to from this project

## Open items

- Verify that SBB backend teams use Spring Boot (job ads).
- Verify HERMES phase and document names at hermes.admin.ch.
