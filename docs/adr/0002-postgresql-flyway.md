# ADR 0002: PostgreSQL with Flyway, content seeded by migrations

Date: 2026-10-15 · Status: accepted

## Context

The data (projects, translations, images, technologies, certifications, contact messages) is tabular. A graph database was considered but adds a second server and memory cost on a small VPS. There is no admin area in Release 1.

## Decision

- PostgreSQL is the system of record, run in Docker locally and in production.
- Flyway versions the schema (`V1__create_schema.sql`) and seeds content (`V2__seed_projects.sql`, ...).
- Translations live in separate tables, so adding FR/IT later is a data change.
- A graph-backed "skills graph" may follow later as a separate experiment.

## Consequences

- Never edit an applied migration; every content change is a new `V<n>__...sql`.
- Content edits need a deploy until the admin area exists.
