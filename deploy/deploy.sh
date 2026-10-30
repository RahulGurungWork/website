#!/usr/bin/env bash
# Deploys one commit on the VPS. Installed as ~/bin/portfolio-deploy and run by GitHub Actions through a deploy key
# that may run nothing else (forced command in ~/.ssh/authorized_keys, see docs/hermes/07-go-live.md):
#   tar -czf - -C deploy docker-compose.yml Caddyfile | ssh ubuntu@<server> <commit sha>
# By hand on the server (same tarball on stdin):  ~/bin/portfolio-deploy <commit sha> < config.tar.gz
#
# Steps: take the compose file and Caddyfile of that commit, pull its image, dump the database (Flyway migrations
# only go forward), switch APP_IMAGE in .env, restart what changed, wait until the site answers.
# Rollback: the previous image is in deployed.log; put it back into APP_IMAGE, `docker compose up -d app`, and
# restore the dump from ~/backups if a migration has to be undone.
set -euo pipefail

SHA="${SSH_ORIGINAL_COMMAND:-${1:-}}"
if ! [[ "$SHA" =~ ^[0-9a-f]{40}$ ]]; then
	echo "usage: portfolio-deploy <40-character commit sha>, config tarball on stdin" >&2
	exit 2
fi
IMAGE="ghcr.io/rahulgurungwork/portfolio:$SHA"
SITE="www.portfolio.rahul.gurung.ch"
cd ~/portfolio/deploy

echo "--- config files from $SHA"
config="$(mktemp -d)"
trap 'rm -rf "$config"' EXIT
# Only these two names are taken from the tarball; .env never leaves or enters the server this way
tar -xzf - -C "$config" docker-compose.yml Caddyfile
caddy_before="$(sha256sum Caddyfile | cut -d' ' -f1)"
install -m 644 "$config/docker-compose.yml" "$config/Caddyfile" .

echo "--- pull $IMAGE"
docker pull --quiet "$IMAGE"

echo "--- database dump"
mkdir -p ~/backups
dump=~/backups/portfolio-before-"${SHA:0:7}"-"$(date +%Y%m%d-%H%M)".dump
docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom' > "$dump" </dev/null
chmod 600 "$dump"
echo "$dump ($(du -h "$dump" | cut -f1))"

echo "--- switch app image"
previous="$(sed -n 's/^APP_IMAGE=//p' .env)"
if [ -n "$previous" ]; then
	sed -i "s|^APP_IMAGE=.*|APP_IMAGE=$IMAGE|" .env
else
	echo "APP_IMAGE=$IMAGE" >> .env
fi
echo "$(date -Iseconds) $IMAGE (before: ${previous:-none})" >> deployed.log
docker compose up -d --remove-orphans </dev/null
if [ "$(sha256sum Caddyfile | cut -d' ' -f1)" != "$caddy_before" ]; then
	echo "--- Caddyfile changed, reloading Caddy"
	docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile </dev/null
fi

echo "--- wait for the site"
for _ in $(seq 1 60); do
	code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 --resolve "$SITE:443:127.0.0.1" "https://$SITE/en" || true)"
	if [ "$code" = "200" ]; then
		echo "site answers 200"
		break
	fi
	sleep 2
done
if [ "$code" != "200" ]; then
	echo "site did not answer 200 within 2 minutes (last: $code)" >&2
	docker compose logs --tail 50 app </dev/null >&2
	exit 1
fi

echo "--- clean up"
# Keep the current and the previous image for a quick rollback, and dumps from the last 30 days
docker images --format '{{.Repository}}:{{.Tag}}' ghcr.io/rahulgurungwork/portfolio \
	| grep -vxF -e "$IMAGE" -e "$previous" | xargs -r docker rmi --force >/dev/null || true
docker image prune --force >/dev/null
find ~/backups -name 'portfolio-before-*.dump' -mtime +30 -delete
echo "deployed $SHA"
