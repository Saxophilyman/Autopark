#!/usr/bin/env bash
set -euo pipefail

# MODE B: Grafana -> notify webhook -> Telegram
# Скрипт можно запускать из любой директории.
# Важно: сам файл скрипта лучше держать в КОРНЕ репозитория.

usage() {
  echo "Usage: $0 --token <TOKEN> --username <USERNAME> [--chatId <CHAT_ID>] [--internalToken <TOKEN>] [--autoparkUrl <URL>] [--port <PORT>]"
  exit 1
}

TOKEN=""
USERNAME=""
CHAT_ID="0"
INTERNAL_TOKEN="dev-token"
AUTOPARK_URL="http://localhost:8080"
PORT="8083"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --token) TOKEN="${2:-}"; shift 2;;
    --username) USERNAME="${2:-}"; shift 2;;
    --chatId) CHAT_ID="${2:-0}"; shift 2;;
    --internalToken) INTERNAL_TOKEN="${2:-dev-token}"; shift 2;;
    --autoparkUrl) AUTOPARK_URL="${2:-http://localhost:8080}"; shift 2;;
    --port) PORT="${2:-8083}"; shift 2;;
    *) usage;;
  esac
done

[[ -n "$TOKEN" && -n "$USERNAME" ]] || usage

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$SCRIPT_DIR"
NOTIFY_DIR="$REPO_ROOT/notify-service"

[[ -d "$NOTIFY_DIR" ]] || { echo "notify-service not found at: $NOTIFY_DIR"; exit 1; }

# --- 1) РЕЖИМ / ФИЧИ ---
export APP_KAFKA_ENABLED="false"

export NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED="true"
export NOTIFY_FEATURES_AUTH_SESSIONS_ENABLED="false"
export NOTIFY_FEATURES_VEHICLE_EVENTS_ENABLED="false"
export NOTIFY_FEATURES_REPORTS_ENABLED="false"
export NOTIFY_FEATURES_MONITORING_EVENTS_ENABLED="false"

# --- 2) TELEGRAM ---
export TELEGRAM_ENABLED="true"
export TELEGRAM_BOT_POLLING_ENABLED="true"

export TELEGRAM_BOT_TOKEN="$TOKEN"
export TELEGRAM_BOT_USERNAME="$USERNAME"
export TELEGRAM_ALERT_CHAT_ID="$CHAT_ID"

# --- 3) INTERNAL TOKEN ---
export INTERNAL_API_TOKEN="$INTERNAL_TOKEN"

# --- 4) AUTOPARK URL ---
export AUTOPARK_BASE_URL="$AUTOPARK_URL"

# --- 5) PORT ---
export SERVER_PORT="$PORT"

echo "=== notify-service: MODE B ==="
echo "RepoRoot: $REPO_ROOT"
echo "NotifyDir: $NOTIFY_DIR"
echo "SERVER_PORT=$SERVER_PORT"
echo "APP_KAFKA_ENABLED=$APP_KAFKA_ENABLED"
echo "NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED=$NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED"
echo "TELEGRAM_ENABLED=$TELEGRAM_ENABLED, TELEGRAM_BOT_POLLING_ENABLED=$TELEGRAM_BOT_POLLING_ENABLED"
echo "TELEGRAM_ALERT_CHAT_ID=$TELEGRAM_ALERT_CHAT_ID"
echo "INTERNAL_API_TOKEN=$INTERNAL_API_TOKEN"
echo "AUTOPARK_BASE_URL=$AUTOPARK_BASE_URL"
echo "Health: http://localhost:$PORT/actuator/health"
echo "Grafana webhook: http://localhost:$PORT/api/notify/grafana"
echo "=============================="

pushd "$NOTIFY_DIR" >/dev/null
mvn spring-boot:run
popd >/dev/null
