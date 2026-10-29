# ADR 0005: Admin panel login with password and authenticator app

Date: 2026-10-29 · Status: accepted (implemented in step A1)

## Context

Release 1 needs a code change and a redeploy for every new project or certificate. The admin panel (FR5, FR9) lets
Rahul edit content in the browser. It is reachable from the public internet, so a stolen or guessed password must not
be enough to get in. There is exactly one admin; no visitor accounts are planned.

## Decision

- Spring Security, one admin account, no user table. The server's `deploy/.env` holds `ADMIN_USERNAME`,
  `ADMIN_PASSWORD_HASH` (BCrypt) and `ADMIN_TOTP_SECRET` (Base32). Plain passwords are never stored or committed.
- One login form at `/{lang}/admin/login`: username, password and the 6-digit code from an authenticator app
  (TOTP, RFC 6238, 30-second steps, one step of clock drift allowed, a used code is not accepted twice).
  TOTP is implemented in the app with `javax.crypto` and tested against the RFC 6238 test vectors, so no extra
  library is needed.
- Brute-force protection: 5 failed logins from one IP address in 15 minutes lock that address for 15 minutes; 20
  failed logins from all addresses lock the account for 15 minutes (higher, so one attacker cannot easily lock the
  admin out). The error message never says which field was wrong.
- Two filter chains: the admin chain (`/en/admin/**`, `/de/admin/**`, `/admin/**`) has login, session and CSRF;
  the public chain has no session, no CSRF token and no extra headers, so public pages still set no cookie
  (privacy notice).
- Session: cookie `Secure`, `HttpOnly`, `SameSite=Strict`, 30 minutes idle timeout, new session ID after login.
  CSRF protection on for every form, public ones included.
- Setup helper `AdminSetup` (run on the PC with the Maven wrapper) prints a password hash, a new TOTP secret and an
  `otpauth://` link for the authenticator app. Rahul copies the values into the server's `.env` himself.
- Admin pages: `noindex`, `Cache-Control: no-store`, excluded in `robots.txt`, not linked from the public site.
  Public pages keep their current caching and headers.

## Alternatives

| Option | Why not |
| :--- | :--- |
| Password only | One leaked password would be enough |
| Caddy basic auth in front | Second password, no lockout, clumsy in the browser |
| Login via GitHub (OAuth) | Depends on an outside service and adds callback setup for one user |

## Consequences

- Losing the authenticator app means generating a new secret with `AdminSetup` and changing `.env` on the server
  (SSH key still needed), which is the recovery path.
- Adding Spring Security changes defaults for the whole app (CSRF, cache headers); the existing tests must keep
  passing and the public pages must keep their Lighthouse scores.
