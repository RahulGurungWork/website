# Test concept

## Test levels

| Level | Checks | Tools | Runs |
| :--- | :--- | :--- | :--- |
| Unit | One class in isolation (validation, spam guard, locale resolution) | JUnit 5, Mockito, AssertJ | Every push |
| Feature | One request through the app | Spring MockMvc | Every push |
| Integration | App against real PostgreSQL (migrations, queries, seed data) | `@SpringBootTest` + Testcontainers | Every push |
| E2E | Real browser through key flows | Playwright for Java | Every push on built container; smoke after deploy |
| Accessibility | Rule checks + manual review | axe-core in Playwright; NVDA on Windows | CI; manual before launch |
| Performance | Page speed and weight | Lighthouse CI | Every push on key pages |

Naming: `*Test` for unit/feature, `*IT` for integration. No global coverage gate; aim for about 80% on the service layer.

## Acceptance criteria

| Req | Criterion | Level |
| :--- | :--- | :--- |
| FR1 | Projects page lists published projects by `sort_order` in the selected language; unpublished never appear | Feature, Integration |
| FR2 | Detail page shows title, description, technologies, links, images; unknown slug returns a styled 404 | Feature, E2E |
| FR3 | Valid submission is saved, emailed, confirmed; invalid input shows field errors and keeps values | Feature, E2E |
| FR3 | Honeypot or more than N requests/minute rejected without saying why | Unit, Feature |
| FR3 | Email failure keeps the message with `mail_sent = false` | Integration |
| FR4 | Certifications show issuer, date, credential link | Feature |
| FR6/FR11 | Every page under /en and /de; switcher keeps the equivalent page; / redirects by browser language, default EN | Feature, E2E |
| FR6 | Every published project has EN and DE translations | Integration |
| FR12/FR13 | Impressum and privacy notice linked in every footer, both languages | E2E |
| NFR3 | HTTP → HTTPS; security headers; secret scan green | Smoke, CI |
| NFR5 | Fresh clone: `mvnw verify` passes | CI |

Also: Flyway migrations apply cleanly to an empty database.

## Targets

- Accessibility: WCAG 2.1 AA; axe-core zero violations; Lighthouse accessibility ≥ 95.
- Performance (mobile): Lighthouse ≥ 90, LCP ≤ 2.5 s, INP ≤ 200 ms, CLS ≤ 0.1; images in WebP/AVIF.
- Browsers: latest two versions of Chrome, Firefox, Safari, Edge; mobile Safari and Chrome.

## Manual accessibility checklist (before launch)

- [ ] Keyboard-only use with visible focus
- [ ] Logical headings and a skip link
- [ ] Contrast ≥ 4.5:1 (3:1 large text)
- [ ] Usable at 200% zoom and on narrow screens
- [ ] Meaningful alt text in EN and DE; decorative images `alt=""`
- [ ] Visible labels; errors announced and linked to fields
- [ ] Correct `lang` attribute per page
- [ ] Screen reader pass with NVDA
