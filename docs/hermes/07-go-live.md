# Go-live checklist (M8)

HERMES phase: Deployment. Order matters: each block only needs the blocks above it. Hosting decision and server
details: [ADR 0004](../adr/0004-hosting-infomaniak.md).

Status 2026-10-23: the site is live at <https://www.portfolio.rahul.gurung.ch> from commit `355371c`, deployed by hand
(image built on the server). Blocks 1–4 are done except the mail password; CD, backups and monitoring are open.

Rehearsed on 2026-10-18 without a server: the production image builds, starts with profile `prod` against an empty
database, applies all migrations, stays healthy without SMTP, and passes `deploy/smoke-test.sh`. `deploy/Caddyfile`
is valid.

## 1. Before ordering

- [x] Infomaniak account: two-factor authentication on (confirmed by Rahul 2026-10-23)
- [ ] Domain `gurung.ch`: turn auto-renew on (probably off on 2026-10-23); confirm on the invoice whether 1 or 2
  years were paid (the Manager shows expiry 2027-10-15)
- [x] Live VPS Lite price checked 2026-10-23: CHF 7.20/month excl. VAT, CHF 93.40/year incl. VAT; with the domain
  about CHF 8.40/month (NFR4 ≤ 15)

## 2. Server

- [x] VPS Lite "portfolio" ordered 2026-10-23, Ubuntu 24.04 LTS, Swiss data centre; IPv4 `179.237.124.160`, IPv6
  `2001:1600:18:20c::3de`
- [x] Log in with an SSH key (user `ubuntu`, sudo)
- [ ] User `deploy` in group `docker` for GitHub Actions (until then everything runs as `ubuntu`)
- [x] SSH: password login off (`passwordauthentication no`)
- [ ] SSH: `PermitRootLogin no` (the cloud image allows root with a key: `without-password`)
- [x] Firewall: ufw allows only 22, 80, 443; Infomaniak firewall opened for TCP 80 and 443
- [x] Automatic security updates (`unattended-upgrades` was already on); timezone Europe/Zurich
- [x] Docker Engine 29.9 and Compose plugin from Docker's apt repository; container logs rotated (3 × 10 MB)

## 3. DNS (Infomaniak, zone `gurung.ch`)

- [x] `A` + `AAAA` for `www.portfolio.rahul` and `portfolio.rahul` (Caddy redirects the second to `www.`)
- [x] Resolves worldwide (checked with Cloudflare and Google DNS). The gibb school network's DNS still answered
  "non-existent" on 2026-10-23; check from another network

## 4. Files and secrets on the server

- [x] Code in `~/portfolio` (from `git archive`), stack runs from `~/portfolio/deploy`
- [x] `~/portfolio/deploy/.env`, `chmod 600`, generated `POSTGRES_PASSWORD` = `DB_PASSWORD`, `APP_IMAGE=portfolio:local`
- [x] `MAIL_PASSWORD` set by Rahul on 2026-10-23 (device password for `rahul@gurung.ch`); app restarted and sees it
- [ ] Registry access for CD: GitHub token with only `read:packages`, then `docker login ghcr.io -u RahulGurungWork`

## 5. GitHub

- [ ] New SSH key pair only for deployments; public key into `~deploy/.ssh/authorized_keys`
- [ ] Repository → Settings → Environments → `production`, secrets: `DEPLOY_HOST`, `DEPLOY_USER` (`deploy`),
  `DEPLOY_SSH_KEY` (the private key)

## 6. First deploy

- [x] First deploy by hand on 2026-10-23 (`945a799`), redeploy with Skills, About, CV and real project texts the same
  day (`355371c`); migrations V1–V6 applied
- [x] Checked from outside: every page 200 in both languages, unknown page 404, `/actuator` 404, `/` → `/en`,
  `portfolio.` → `www.`, HTTP → HTTPS, security headers, both CV PDFs
- [ ] Actions → Deploy → Run workflow on `main` (needs blocks 4 and 5); it also runs `deploy/smoke-test.sh`
- [ ] Send yourself a message through the **live** contact form; it should arrive at `rahul@gurung.ch`. The test on
  2026-10-23 went to the local copy (Mailpit), not the live site: the live database had no messages. Test from a
  network where the address resolves (not the gibb network)
- [ ] Open the site on a phone, in both languages

Redeploy by hand until CD works: on the PC `git archive --format=tar.gz -o portfolio.tar.gz HEAD`, `scp` it to the
server, unpack into a fresh `~/portfolio` keeping `deploy/.env`, then `docker build -t portfolio:local .` and
`cd deploy && docker compose up -d app`.

## 7. After the first deploy

- [ ] Caddy: enable HSTS (`Strict-Transport-Security "max-age=31536000"`); deploy again
- [ ] Deploy workflow: switch the trigger to `push` on `main`
- [ ] Backups: cron for `deploy/backup.sh` (see the script header)
- [ ] Off-site copy: to Rahul's PC (decided 2026-10-23), pulled from the server with `scp`/`rsync`; the PC must
  run it regularly (Windows scheduled task) and keep it out of Git
- [ ] Restore test: `pg_restore` the newest dump into a scratch database and check the row counts
- [ ] Uptime monitor: a free external service that emails `rahul@gurung.ch` (approved 2026-10-23); Rahul creates the
  account and adds `https://www.portfolio.rahul.gurung.ch/en`
- [ ] Privacy notice: compare with the live setup (no access logs in Caddy, Infomaniak only); update the date if
  anything changed
- [ ] Delete the old placeholder in `~/placeholder` on the server
- [ ] Roadmap: mark M8 done; note the go-live date in `05-roadmap.md`
