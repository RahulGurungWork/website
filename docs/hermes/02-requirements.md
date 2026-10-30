# Scope and requirements

HERMES phase: Concept.

## Functional requirements

| ID | Requirement | Priority | Release |
| :--- | :--- | :--- | :--- |
| FR1 | Visitors can browse a list of current and past projects | Must | 1 |
| FR2 | Each project has a detail page (title, description, technologies, links, images) | Must | 1 |
| FR3 | Visitors can send a message via the contact form, with spam protection | Must | 1 |
| FR4 | Certifications page | Must | 1 |
| FR5 | Admin login; admin can create, edit, hide and delete projects | Must | 2 (admin panel, 2026-10-30) |
| FR6 | English and German content | Must | 1 |
| FR7 | French and Italian content | Should | later |
| FR8 | Romansh content | Could | later |
| FR9 | Admin can manage certifications | Should | 2 (admin panel, 2026-10-30) |
| FR14 | Admin can read, mark and delete contact messages | Should | 2 (admin panel, 2026-10-30) |
| FR10 | Downloadable CV (PDF) | Could | 1 (added 2026-10-18) |
| FR11 | Language switcher with URL per language (/en, /de) | Must | 1 |
| FR12 | Impressum page | Must | 1 |
| FR13 | Privacy notice page | Must | 1 |
| – | Change log | – | later |

## Non-functional requirements

| ID | Requirement |
| :--- | :--- |
| NFR1 | Accessible: WCAG 2.1 AA |
| NFR2 | Fast on mobile (see test concept) |
| NFR3 | HTTPS only; no secrets in the repository |
| NFR4 | Runs on up to 15 CHF/month |
| NFR5 | Maintainable by one person; `mvnw verify` builds and tests a fresh clone |

## Showcase projects (Release 1)

1. Notion project management board (`notion-workspace-prototype`)
2. n8n workflow: users from an Abacus Excel export into the company application (`n8n-workflow`)
3. Lerndokumentations-Generator, a Python console program (`lerndoku-generator`)

Content is seeded into PostgreSQL through Flyway migrations (texts in V4); the admin area takes over later.
Beyond the original scope, the home page also has Skills and About sections (2026-10-18).
