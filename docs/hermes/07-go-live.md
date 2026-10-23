# Go-live checklist (M8)

HERMES phase: Deployment. Order matters: each block only needs the blocks above it. Hosting decision: [ADR 0004](../adr/0004-hosting-infomaniak.md).

Rehearsed on 2026-10-18 without a server: the production image builds, starts with profile `prod` against an empty
database, applies all migrations, stays healthy without SMTP, and passes `deploy/smoke-test.sh`. `deploy/Caddyfile`
is valid.

## 1. Before ordering

- [ ] Infomaniak account: two-factor authentication on; domain auto-renew on (open item from M1)
- [ ] Check the live VPS Lite price; total stays ≤ 15 CHF/month (NFR4)

## 2. Server

- [ ] Order VPS Lite with Ubuntu 24.04 LTS, Swiss data centre; note its IPv4 (and IPv6) address
- [ ] Log in with an SSH key; create user `deploy`, add it to group `docker` (after step 3)
- [ ] SSH: disable password login and root login (`PasswordAuthentication no`, `PermitRootLogin no`)
- [ ] Firewall: allow only 22, 80, 443 (`ufw allow OpenSSH`, `ufw allow 80,443/tcp`, `ufw enable`)
- [ ] Automatic security updates: `unattended-upgrades`
- [ ] Install Docker Engine and the Compose plugin from Docker's apt repository

## 3. DNS (Infomaniak, zone `gurung.ch`)

- [ ] `A` record `www.portfolio.rahul` → VPS IPv4 (plus `AAAA` → IPv6 if the VPS has one)
- [ ] `A` record `portfolio.rahul` → same address (Caddy redirects it to `www.`)
- [ ] Wait until `nslookup www.portfolio.rahul.gurung.ch` returns the VPS address

## 4. Files and secrets on the server

- [ ] Copy `deploy/docker-compose.yml`, `deploy/Caddyfile` and `deploy/backup.sh` to `~/portfolio/deploy/`
- [ ] Create `~/portfolio/deploy/.env` from `deploy/.env.example`; `chmod 600 .env`
  - long random `POSTGRES_PASSWORD`, same value in `DB_PASSWORD`
  - `MAIL_PASSWORD`: the `rahul@gurung.ch` mailbox password (or an app password, if kSuite offers one)
- [ ] Registry access: the image on GHCR is private by default. Create a GitHub token with only `read:packages`,
  then on the server: `docker login ghcr.io -u RahulGurungWork`

## 5. GitHub

- [ ] New SSH key pair only for deployments; public key into `~deploy/.ssh/authorized_keys`
- [ ] Repository → Settings → Environments → `production`, secrets: `DEPLOY_HOST`, `DEPLOY_USER` (`deploy`),
  `DEPLOY_SSH_KEY` (the private key)

## 6. First deploy

- [ ] Actions → Deploy → Run workflow on `main`. It builds and pushes the image, starts the stack over SSH and runs
  `deploy/smoke-test.sh https://www.portfolio.rahul.gurung.ch` (pages, language redirect, HTTPS redirect,
  security headers, actuator hidden)
- [ ] Send yourself a message through the live contact form; it should arrive at `rahul@gurung.ch`
- [ ] Open the site on a phone, in both languages

## 7. After the first deploy

- [ ] Caddy: enable HSTS (`Strict-Transport-Security "max-age=31536000"`) once HTTPS works; deploy again
- [ ] Deploy workflow: switch the trigger to `push` on `main`
- [ ] Backups: cron for `deploy/backup.sh` (see the script header); copy the backup folder off the server
- [ ] Restore test: `pg_restore` the newest dump into a scratch database and check the row counts
- [ ] Privacy notice: compare with the live setup (no access logs in Caddy, Infomaniak only); update the date if
  anything changed
- [ ] Roadmap: mark M8 done; note the go-live date in `05-roadmap.md`
