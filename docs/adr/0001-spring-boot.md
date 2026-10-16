# ADR 0001: Spring Boot with Thymeleaf

Date: 2026-10-15 · Status: accepted

## Context

The site must show backend skills relevant to SBB, run on ≤ 15 CHF/month, and later gain an admin area. Candidates: Laravel, ASP.NET Core (Razor Pages), Spring Boot + Thymeleaf. A static site generator was set aside because it shows no backend skills and the admin area would force a rebuild.

| Criterion | Weight | Laravel | ASP.NET Core | Spring Boot |
| :--- | :-: | :-: | :-: | :-: |
| Learning curve | 15 | 2 | 4 | 3 |
| Hosting cost | 15 | 5 | 3 | 2 |
| Admin tooling (later) | 15 | 5 | 2 | 2 |
| Recruiter perception (SBB) | 20 | 2 | 4 | 5 |
| Performance | 10 | 3 | 5 | 3 |
| Maintainability | 15 | 4 | 4 | 4 |
| i18n and accessibility | 10 | 4 | 4 | 4 |
| **Weighted score** | 100 | 3.50 | 3.65 | 3.35 |

## Decision

Spring Boot monolith with server-rendered Thymeleaf pages, layered architecture (Controller → Service → Repository), package-by-feature. Build tool: Maven. CSS: Bootstrap, served from `static/`.

## Consequences

- The matrix ranks ASP.NET Core higher; the choice rests on recruiter perception and the unverified assumption that SBB backend teams use Spring Boot. Correction from the planning chat: raising the learning-curve score to 4 gives 3.50, not 3.7.
- Java needs a VPS rather than shared hosting.
- Spring Security is deferred to the admin release.
