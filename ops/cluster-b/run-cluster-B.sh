#!/usr/bin/env bash
# ops/cluster-b/run-cluster-B.sh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENV_FILE="$ROOT_DIR/.env"
ENV_EXAMPLE="$ROOT_DIR/.env.example"
COMPOSE_FILE="$ROOT_DIR/docker-compose.cluster-b.yml"

require_file() {
  local path="$1"
  local name="$2"
  if [[ ! -f "$path" ]]; then
    echo "Не найден файл: $name ($path)" >&2
    exit 1
  fi
}

require_file "$COMPOSE_FILE" "docker-compose.cluster-b.yml"
require_file "$ENV_EXAMPLE" ".env.example"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo ""
  echo "Создан ops/cluster-b/.env из .env.example."
  echo "Заполни минимум TELEGRAM_BOT_TOKEN и TELEGRAM_ALERT_CHAT_ID, затем запусти скрипт снова."
  echo ""
  exit 1
fi

docker compose version >/dev/null

set -a
source "$ENV_FILE"
set +a

cd "$ROOT_DIR"
docker compose --env-file ./.env -f ./docker-compose.cluster-b.yml up -d --build

AUTOPARK_PORT="${AUTOPARK_PORT:-8080}"
NOTIFY_PORT="${NOTIFY_PORT:-8082}"
PROMETHEUS_PORT="${PROMETHEUS_PORT:-9090}"
GRAFANA_PORT="${GRAFANA_PORT:-3000}"
INTERNAL_API_TOKEN="${INTERNAL_API_TOKEN:-dev-token}"

echo ""
echo "Кластер B поднят."
echo "Autopark:    http://localhost:${AUTOPARK_PORT}"
echo "Notify:      http://localhost:${NOTIFY_PORT}"
echo "Prometheus:  http://localhost:${PROMETHEUS_PORT}"
echo "Grafana:     http://localhost:${GRAFANA_PORT}"
echo ""
echo "Smoke-test: имитация webhook от Grafana -> notify-service (должно уйти в Telegram):"
echo "  curl -X POST \"http://localhost:${NOTIFY_PORT}/api/grafana/webhook\" \\"
echo "    -H \"Authorization: Bearer ${INTERNAL_API_TOKEN}\" \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"title\":\"smoke\",\"state\":\"alerting\",\"message\":\"cluster-b test\"}'"
echo ""
