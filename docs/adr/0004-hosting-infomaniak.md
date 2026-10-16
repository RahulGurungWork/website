# ADR 0004: Hosting at Infomaniak, single VPS with Docker Compose

Date: 2026-10-15 · Status: accepted

## Context

Spring Boot needs a Java runtime, which rules out PHP shared hosting. Budget is ≤ 15 CHF/month. Domain gurung.ch is already registered at Infomaniak.

## Decision

- One Infomaniak VPS Lite (Swiss datacenter), 4 GB RAM if the live price fits, otherwise 2 GB.
- Docker Compose stack: Caddy (HTTPS, reverse proxy) → Spring Boot app → PostgreSQL.
- Main hostname `www.portfolio.rahul.gurung.ch`; `portfolio.rahul.gurung.ch` redirects to it.
- Mail through Infomaniak authenticated SMTP (`mail.infomaniak.com`, port 587, STARTTLS) with the free kSuite mailbox `rahul@gurung.ch`, which sends and receives the contact form mail (SMTP send tested 2026-10-16).
- CD via GitHub Actions → GHCR → SSH deploy. Nightly `pg_dump` with an off-site copy (Swiss Backup candidate).
- Order the VPS at the start of M7/M8, not earlier.

## Consequences

- VPS Lite has no snapshots, so the backup and restore test matter.
- Swiss data location goes into the privacy notice.
- Prices are unverified; check live before ordering.
