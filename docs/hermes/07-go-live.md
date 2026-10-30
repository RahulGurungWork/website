# Go-live checklist (M8)

HERMES phase: Deployment. Order matters: each block only needs the blocks above it. Hosting decision and server
details: [ADR 0004](../adr/0004-hosting-infomaniak.md).

Status 2026-10-23: the site is live at <https://www.portfolio.rahul.gurung.ch> from commit `355371c`, deployed by hand
(image built on the server). Blocks 1–4 are done. Automatic deploys set up on 2026-10-30 (blocks 5 and 6);
backups and monitoring are open.

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
- [x] ~~User `deploy` in group `docker`~~ Replaced on 2026-10-30 by a deploy key on `ubuntu` that can only run
  `~/bin/portfolio-deploy` (`restrict,command=...` in `authorized_keys`). A `deploy` user in group `docker` would have
  had root-equivalent rights and a shell
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
- [x] ~~Registry access with a `read:packages` token~~ Not needed: the image `ghcr.io/rahulgurungwork/portfolio` is
  public (decided 2026-10-30; it only contains code that is public on GitHub, all secrets stay in `.env`)

## 5. GitHub

- [ ] New SSH key pair only for deployments, without passphrase (`ssh-keygen -t ed25519 -f ~/.ssh/portfolio_deploy`)
- [ ] On the server: copy `deploy/deploy.sh`, `deploy/install-deploy-key.sh` and the public key to `~`, run
  `bash install-deploy-key.sh`. Check: `ssh -i ~/.ssh/portfolio_deploy ubuntu@179.237.124.160 whoami` must only
  print the usage line of `portfolio-deploy`
- [ ] Repository → Settings → Environments → `production`, secret `DEPLOY_SSH_KEY` (the private key); then delete the
  private key on the PC. Host and user are in the workflow, the host keys are pinned in `deploy/known_hosts`
- [ ] After the first image build: Packages → `portfolio` → Package settings → Change visibility → Public

## 6. First deploy

- [x] First deploy by hand on 2026-10-23 (`945a799`), redeploy with Skills, About, CV and real project texts the same
  day (`355371c`); migrations V1–V6 applied
- [x] Checked from outside: every page 200 in both languages, unknown page 404, `/actuator` 404, `/` → `/en`,
  `portfolio.` → `www.`, HTTP → HTTPS, security headers, both CV PDFs
- [ ] First automatic deploy: push to `main`, CI passes, Deploy runs (needs blocks 4 and 5); it also runs
  `deploy/smoke-test.sh`
- [x] Live contact form: test message sent 2026-10-23 13:57 (name "Test"), stored as message 1 with `mail_sent = true`,
  no mail errors in the app log, so Infomaniak SMTP accepted it. Arrival in the inbox confirmed by Rahul (an
  earlier test that day had gone to the local Mailpit by mistake)
- [ ] Open the site on a phone, in both languages

How a deploy works since 2026-10-30: every push to `main` runs CI; when CI passes, the Deploy workflow builds the
image, pushes it to ghcr.io as `:<commit sha>` and `:latest`, sends `docker-compose.yml` and `Caddyfile` of that
commit to the server and runs `~/bin/portfolio-deploy <sha>` there (`deploy/deploy.sh`). The script dumps the
database to `~/backups/portfolio-before-<sha>-<time>.dump` (kept 30 days), sets `APP_IMAGE` in `.env`, restarts,
reloads Caddy if the Caddyfile changed, and waits until the site answers. Then the workflow runs the smoke test.
Changes to `deploy.sh` itself are installed by hand (copy it to `~/bin/portfolio-deploy`).

Rollback: `~/portfolio/deploy/deployed.log` on the server lists each deploy with the image before it. Put that image
back into `APP_IMAGE` in `.env` and run `docker compose up -d app`. If a migration has to be undone as well, restore
the dump taken before that deploy. Redeploy without a new commit: Actions → Deploy → Run workflow.

## 7. After the first deploy

- [ ] Caddy: enable HSTS (`Strict-Transport-Security "max-age=31536000"`); deploy again
- [ ] Deploy workflow runs after every passing CI run on `main` (set up 2026-10-30; tick after the first one)
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
