#!/usr/bin/env bash
# Post-deploy smoke test. Checks the live site from outside, as a visitor sees it.
#   ./deploy/smoke-test.sh https://www.portfolio.rahul.gurung.ch
#   ./deploy/smoke-test.sh http://localhost:8080      (app only: proxy checks are skipped)
# Exits non-zero on the first failure.
set -euo pipefail

BASE="${1:?usage: smoke-test.sh <base-url>}"
BASE="${BASE%/}"
failures=0

pass() { printf '  ok    %s\n' "$1"; }
fail() { printf '  FAIL  %s\n' "$1"; failures=$((failures + 1)); }

# status <path> [extra curl args...] -> HTTP status code
status() {
	local path="$1"; shift
	curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$@" "$BASE$path"
}

expect_status() {
	local expected="$1" path="$2"; shift 2
	local actual
	actual="$(status "$path" "$@")"
	if [ "$actual" = "$expected" ]; then pass "$path -> $actual"; else fail "$path -> $actual (expected $expected)"; fi
}

expect_body() {
	local path="$1" needle="$2"
	if curl -s --max-time 15 "$BASE$path" | grep -qF "$needle"; then pass "$path contains $needle"; else fail "$path lacks $needle"; fi
}

echo "Smoke test: $BASE"

# Pages, both languages
expect_status 200 /en
expect_body /en '<html lang="en"'
expect_status 200 /de
expect_body /de '<html lang="de"'
expect_status 200 /en/projects
expect_status 200 /de/projects/n8n-workflow
expect_status 200 /en/certificates
expect_status 200 /de/contact
expect_status 200 /en/legal-notice
expect_status 200 /de/privacy
expect_status 404 /en/does-not-exist

# / redirects by browser language
location="$(curl -s -o /dev/null -w '%{redirect_url}' -H 'Accept-Language: de-CH,de;q=0.9' "$BASE/")"
if [[ "$location" == */de ]]; then pass "/ (de) -> $location"; else fail "/ (de) -> '$location' (expected .../de)"; fi

# Hashed stylesheet is served with a long cache lifetime
css="$(curl -s "$BASE/en" | grep -oE '/css/site-[0-9a-f]+\.css' | head -n 1 || true)"
if [ -n "$css" ] && curl -s -D - -o /dev/null "$BASE$css" | grep -qi '^cache-control:.*max-age=31536000'; then
	pass "$css cached for a year"
else
	fail "hashed stylesheet missing or not cacheable ('$css')"
fi

# Behind Caddy (https only): HTTPS redirect, security headers, actuator hidden
if [[ "$BASE" == https://* ]]; then
	host="${BASE#https://}"
	redirect="$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' "http://$host/en")"
	if [[ "$redirect" =~ ^30[178]\ https:// ]]; then pass "http -> https ($redirect)"; else fail "http -> '$redirect' (expected redirect to https)"; fi

	headers="$(curl -s -D - -o /dev/null "$BASE/en")"
	for header in 'x-content-type-options: nosniff' 'referrer-policy: strict-origin-when-cross-origin' 'x-frame-options: deny'; do
		if grep -qi "^$header" <<<"$headers"; then pass "header $header"; else fail "header $header missing"; fi
	done
	if grep -qi '^server:' <<<"$headers"; then fail "Server header exposed"; else pass "no Server header"; fi

	expect_status 404 /actuator/health
fi

if [ "$failures" -gt 0 ]; then
	echo "$failures check(s) failed"
	exit 1
fi
echo "All checks passed"
