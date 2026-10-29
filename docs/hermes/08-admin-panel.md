# Admin panel (Release 2)

HERMES phase: Concept. Status: plan, 2026-10-29. Login decision: [ADR 0005](../adr/0005-admin-login.md).

The admin panel lets Rahul manage the site content in the browser instead of writing Flyway scripts and redeploying.

## Scope

| ID | Requirement | In this release |
| :--- | :--- | :--- |
| FR5 | Admin login; create, edit, publish/hide, delete and reorder projects | Yes |
| FR9 | Create, edit and delete certifications | Yes |
| FR14 | Read contact messages, see whether the email was sent, mark as read, delete | Yes (new) |
| – | Image uploads for project screenshots | Later |

Admin pages exist in English and German (`/en/admin/…`, `/de/admin/…`) with the same language switcher as the public
site.

## What the admin can edit

- **Project:** slug, status (current/past), start and end date, repository and demo link, order, published,
  technologies (pick existing or add new), title, summary and description in EN and DE (both required to publish).
- **Certification:** name, issuer, date, category, credential link, order.
- **Message:** read only; mark as read, delete. Needs a new column `read_at` (migration V7).

## Steps (one 3-hour session each)

| Step | Content | Done when |
| :--- | :--- | :--- |
| A1 | Spring Security, login page EN/DE with password + TOTP code, lockout, logout, `AdminSetup` helper, empty dashboard | Login works locally with an authenticator app; all public tests still pass. Built 2026-10-29 on branch `feature/a1-admin-login` |
| A2 | Projects: list, create, edit, delete, publish/hide, order, technologies, EN + DE texts | A new project appears on the public site without a deploy |
| A3 | Certifications: list, create, edit, delete | Changes show on the certificates page and in the CV |
| A4 | Messages: inbox, detail, mark read, delete (V7 `read_at`) | Inbox shows new messages first |
| A5 | Browser tests for login and editing, axe on admin pages, docs, deploy with the new `.env` values | Live admin works over HTTPS; CI green |

## Tests

- Unit: TOTP against the RFC 6238 vectors, code reuse rejected, lockout counter, form validation.
- Feature (MockMvc): every admin URL redirects to the login when logged out; forms without CSRF token are rejected;
  the contact form still works with CSRF.
- Integration: create, edit, publish and delete against PostgreSQL; the public pages reflect the changes.
- Browser + axe: log in with a generated code, create a project, see it on the public page; no WCAG violations on
  admin pages.

## Risks

| Risk | Mitigation |
| :--- | :--- |
| Spring Security changes public behaviour (CSRF, caching) | Full test suite and Lighthouse before deploy |
| Lost authenticator app | Recovery with `AdminSetup` and SSH access to the server |
| Editing mistakes on the live site | Delete asks for confirmation; nightly backup (M8 follow-up) before go-live of the admin |
