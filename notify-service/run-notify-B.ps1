# MODE B: Grafana -> notify webhook -> Telegram
# Скрипт можно запускать из любой директории.
# Важно: сам файл скрипта лучше держать в КОРНЕ репозитория.

param(
  [Parameter(Mandatory=$true)]
  [string]$TelegramBotToken,

  [Parameter(Mandatory=$true)]
  [string]$TelegramBotUsername,

  [string]$TelegramAlertChatId = "0",
  [string]$InternalApiToken = "dev-token",
  [string]$AutoparkBaseUrl = "http://localhost:8080",
  [int]$NotifyPort = 8083
)

# Репозиторий = папка где лежит этот скрипт
$RepoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$NotifyDir = Join-Path $RepoRoot "notify-service"

if (!(Test-Path $NotifyDir)) {
  Write-Error "Не найдена папка notify-service рядом со скриптом. RepoRoot=$RepoRoot"
  exit 1
}

# --- 1) РЕЖИМ / ФИЧИ ---
$env:APP_KAFKA_ENABLED = "false"

$env:NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED = "true"
$env:NOTIFY_FEATURES_AUTH_SESSIONS_ENABLED   = "false"
$env:NOTIFY_FEATURES_VEHICLE_EVENTS_ENABLED  = "false"
$env:NOTIFY_FEATURES_REPORTS_ENABLED         = "false"
$env:NOTIFY_FEATURES_MONITORING_EVENTS_ENABLED = "false"

# --- 2) TELEGRAM ---
$env:TELEGRAM_ENABLED = "true"
$env:TELEGRAM_BOT_POLLING_ENABLED = "true"

$env:TELEGRAM_BOT_TOKEN = $TelegramBotToken
$env:TELEGRAM_BOT_USERNAME = $TelegramBotUsername
$env:TELEGRAM_ALERT_CHAT_ID = $TelegramAlertChatId

# --- 3) INTERNAL TOKEN ---
$env:INTERNAL_API_TOKEN = $InternalApiToken

# --- 4) AUTOPARK URL ---
$env:AUTOPARK_BASE_URL = $AutoparkBaseUrl

# --- 5) PORT ---
$env:SERVER_PORT = "$NotifyPort"

Write-Host "=== notify-service: MODE B ==="
Write-Host "RepoRoot: $RepoRoot"
Write-Host "NotifyDir: $NotifyDir"
Write-Host "SERVER_PORT=$env:SERVER_PORT"
Write-Host "APP_KAFKA_ENABLED=$env:APP_KAFKA_ENABLED"
Write-Host "NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED=$env:NOTIFY_FEATURES_GRAFANA_WEBHOOK_ENABLED"
Write-Host "TELEGRAM_ENABLED=$env:TELEGRAM_ENABLED, TELEGRAM_BOT_POLLING_ENABLED=$env:TELEGRAM_BOT_POLLING_ENABLED"
Write-Host "TELEGRAM_ALERT_CHAT_ID=$env:TELEGRAM_ALERT_CHAT_ID"
Write-Host "INTERNAL_API_TOKEN=$env:INTERNAL_API_TOKEN"
Write-Host "AUTOPARK_BASE_URL=$env:AUTOPARK_BASE_URL"
Write-Host "Health: http://localhost:$NotifyPort/actuator/health"
Write-Host "Grafana webhook: http://localhost:$NotifyPort/api/notify/grafana"
Write-Host "=============================="

Push-Location $NotifyDir
try {
  mvn spring-boot:run
} finally {
  Pop-Location
}
