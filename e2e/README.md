# End-to-end tests

Browser tests with Playwright for Java and axe-core accessibility checks. They live with the other tests in
`src/test/java/ch/gurung/rahul/portfolio/browser/` and run in `mvnw verify` against the real application
(PostgreSQL via Testcontainers) in a headless Chromium.

| Class | Covers |
| :--- | :--- |
| `BrowserFlowsIT` | `/` redirect by browser language; home to project detail in EN and DE; language switcher keeps the page; contact form errors and success; legal notice and privacy notice reachable from every footer; skip link |
| `AccessibilityBrowserIT` | No axe-core WCAG 2.1 A/AA violations on every page type, both languages, light and dark mode, and the contact form with errors |

Playwright downloads Chromium on first use. On Linux it also needs system libraries:

```bash
./mvnw -B -q test-compile
./mvnw -B -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test -Dexec.args="install --with-deps chromium"
```

Playwright 1.52 supports Ubuntu up to 24.04, so CI is pinned to `ubuntu-24.04`.

The post-deploy smoke test is `deploy/smoke-test.sh` (run by the Deploy workflow). Still open: the manual checks in
`docs/hermes/06-test-results.md` (NVDA, keyboard-only use, 200% zoom).
