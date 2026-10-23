# Test results

HERMES phase: Implementation. Status after M7, 2026-10-17. Test levels and criteria: [`04-test-concept.md`](04-test-concept.md).

## Automated (every push, GitHub Actions)

| Level | Suite | Result 2026-10-17 |
| :--- | :--- | :--- |
| Unit / feature | 63 tests (`*Test`), incl. MockMvc for every page | Pass |
| Integration | 23 tests (`*IT`) against PostgreSQL 18 via Testcontainers | Pass |
| E2E | `BrowserFlowsIT`: 11 flows in headless Chromium | Pass |
| Accessibility | `AccessibilityBrowserIT`: axe-core WCAG 2.1 A/AA on 17 pages light, 7 dark, contact form with errors | Pass, 0 violations |
| Performance | Lighthouse CI, mobile, median of 3 runs on 5 pages | See below |
| Secrets | gitleaks | Pass |

Re-run 2026-10-23 on Windows (`mvnw verify`) after merging Skills, About, real project content and the CV
(`355371c`): 66 unit and feature tests and 63 integration tests, including browser flows, axe-core and `CvIT`, all
pass. The same day the live site was checked from outside (pages, redirects, headers, CV PDFs; see
[`07-go-live.md`](07-go-live.md)). Lighthouse below was not re-measured after these changes.

Lighthouse (local run of the packaged app, same config as CI):

| Page | Performance | Accessibility | Best practices | SEO | LCP | CLS | TBT |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `/en` | 100 | 100 | 100 | 100 | 1.1 s | 0 | 0 ms |
| `/de/projects` | 100 | 100 | 100 | 100 | 1.1 s | 0 | 0 ms |
| `/en/projects/n8n-workflow` | 100 | 100 | 100 | 100 | 1.1 s | 0 | 0 ms |
| `/de/certificates` | 100 | 100 | 100 | 100 | 1.1 s | 0 | 0 ms |
| `/en/contact` | 100 | 100 | 100 | 100 | 1.1 s | 0 | 0 ms |

Measured without project screenshots and without self-hosted fonts; re-check when they are added (CI fails below performance 90, accessibility 95, LCP 2.5 s or CLS 0.1).

## Acceptance criteria

| Req | Criterion | Covered by |
| :--- | :--- | :--- |
| FR1 | Published projects by `sort_order` in the selected language; unpublished never appear | `ProjectServiceIT`, `ProjectRepositoryIT`, `ProjectControllerTest` |
| FR2 | Detail page content; unknown slug gives a styled 404 | `ProjectControllerTest`, `ErrorPageAndAssetsIT`, `BrowserFlowsIT` |
| FR3 | Valid submission saved, mailed, confirmed; invalid input shows field errors and keeps values | `ContactControllerTest`, `ContactServiceIT`, `BrowserFlowsIT` |
| FR3 | Honeypot or rate limit rejected without saying why | `SpamGuardTest`, `RateLimiterTest`, `ContactControllerTest` |
| FR3 | Mail failure keeps the message with `mail_sent = false` | `ContactServiceIT` |
| FR4 | Certifications show issuer, date, credential link | `CertificationControllerTest`, `CertificationServiceIT` |
| FR6/FR11 | Every page under /en and /de; switcher keeps the page; / redirects by browser language, default EN | `LanguagesTest`, `HomeControllerTest`, `BrowserFlowsIT` |
| FR6 | Every published project has EN and DE translations | `ProjectRepositoryIT` |
| FR12/FR13 | Legal notice and privacy notice linked in every footer, both languages | `LegalControllerTest`, `BrowserFlowsIT` |
| NFR3 | HTTP → HTTPS, security headers | Open: smoke test after deploy (M8) |
| NFR5 | Fresh clone: `mvnw verify` passes | CI |

## Manual checks (before launch)

To do by hand; tick and date each line. Use Chrome or Edge on Windows with NVDA.

- [ ] Keyboard only: Tab through every page type; focus always visible; skip link works; contact form can be sent
- [ ] Logical headings (NVDA: <kbd>H</kbd> / <kbd>Insert</kbd>+<kbd>F7</kbd>)
- [ ] Contrast: covered by axe in light and dark mode; spot-check the hero and footer
- [ ] 200% browser zoom and a 320 px wide window: no horizontal scrolling, nothing cut off
- [ ] Alt texts in EN and DE once screenshots exist; decorative images `alt=""`
- [ ] Contact form errors: NVDA announces the summary; each error is linked to its field
- [ ] `lang` attribute: NVDA switches voice on the EN/DE switcher and on German pages
- [ ] NVDA pass: home, projects, one project, certificates, contact (with errors and success), legal notice, privacy
