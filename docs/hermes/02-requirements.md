# Scope and requirements

HERMES phase: Concept.

## Functional requirements

| ID | Requirement | Priority | Release |
| :--- | :--- | :--- | :--- |
| FR1 | Visitors can browse a list of current and past projects | Must | 1 |
| FR2 | Each project has a detail page (title, description, technologies, links, images) | Must | 1 |
| FR3 | Visitors can send a message via the contact form, with spam protection | Must | 1 |
| FR4 | Certifications page | Must | 1 |
| FR5 | Admin login; admin can create, edit, hide and delete projects | Must | later |
| FR6 | English and German content | Must | 1 |
| FR7 | French and Italian content | Should | later |
| FR8 | Romansh content | Could | later |
| FR9 | Admin can manage certifications | Should | later |
| FR10 | Downloadable CV (PDF) | Could | later |
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

1. Notion workspace prototype (project management system)
2. n8n workflow process
3. Lerndokumentationsgenerator

Content is seeded into PostgreSQL through Flyway migrations; the admin area takes over later.
