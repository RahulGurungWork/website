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
- Order the VPS at the start of M7/M8, not earlier. Amended 2026-10-23: ordered early to practise deployment and have the domain live with a placeholder; this costs about 2.5 idle months (about CHF 20).

## Server (ordered 2026-10-23)

| Item | Value |
| :--- | :--- |
| Product | Infomaniak VPS Lite "portfolio", 2 vCPU, 4 GB RAM, 60 GB NVMe, Swiss datacenter |
| Price | CHF 86.40 excl. VAT / CHF 93.40 incl. 8.1% VAT for 12 months, paid upfront (CHF 7.20/month excl. VAT) |
| OS | Ubuntu 24.04 LTS, internal hostname `ov-722a37` |
| Addresses | IPv4 `179.237.124.160`, IPv6 `2001:1600:18:20c::3de` |
| Access | User `ubuntu` (sudo), SSH key only: password login and root password login off |
| Firewall | Infomaniak firewall and ufw allow only TCP 22, 80, 443 (plus UDP 443 on ufw) |
| Updates | `unattended-upgrades` on; timezone Europe/Zurich |
| Docker | Engine 29.9 + Compose plugin from Docker's apt repo; container logs rotated (3 × 10 MB) |
| DNS | A + AAAA for `www.portfolio.rahul` and `portfolio.rahul` in the gurung.ch zone |
| Running now | Caddy placeholder page in `~/placeholder` (HTTPS, redirect `portfolio.` → `www.`), replaced in M8 |

## Consequences

- VPS Lite has no snapshots, so the backup and restore test matter.
- VPS Lite resources can only be scaled up, never down.
- Outbound port 25 is blocked; mail goes through Infomaniak SMTP on 587.
- Ports published by Docker containers bypass ufw: never publish the database port.
- Swiss data location goes into the privacy notice.
- Renewal in October 2027; check the renewal price and auto-renew before then.
