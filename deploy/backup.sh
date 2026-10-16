#!/usr/bin/env bash
# Nightly PostgreSQL dump (draft until M8). Run from cron on the VPS, e.g.:
#   15 3 * * * /home/deploy/portfolio/deploy/backup.sh >> /var/log/portfolio-backup.log 2>&1
# Keeps 7 daily and 4 weekly dumps. Copy BACKUP_DIR off the server as well (e.g. Swiss Backup).
set -euo pipefail

cd "$(dirname "$0")"
source .env

BACKUP_DIR="${BACKUP_DIR:-/var/backups/portfolio}"
STAMP="$(date +%F)"
mkdir -p "$BACKUP_DIR/daily" "$BACKUP_DIR/weekly"

docker compose exec -T db pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom \
	> "$BACKUP_DIR/daily/portfolio-$STAMP.dump"

# Sunday: keep a weekly copy
if [ "$(date +%u)" -eq 7 ]; then
	cp "$BACKUP_DIR/daily/portfolio-$STAMP.dump" "$BACKUP_DIR/weekly/"
fi

find "$BACKUP_DIR/daily" -name '*.dump' -mtime +7 -delete
find "$BACKUP_DIR/weekly" -name '*.dump' -mtime +28 -delete
