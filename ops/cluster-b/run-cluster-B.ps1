# ops/cluster-b/run-cluster-B.ps1

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = $PSScriptRoot  # ops/cluster-b
$EnvFile = Join-Path $Root ".env"
$EnvExample = Join-Path $Root ".env.example"
$ComposeFile = Join-Path $Root "docker-compose.cluster-b.yml"

function Require-File([string]$path, [string]$name) {
  if (-not (Test-Path $path)) {
    throw "Не найден файл: $name ($path)"
  }
}

function Read-DotEnv([string]$path) {
  $map = @{}
  Get-Content $path | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }
    $idx = $line.IndexOf("=")
    if ($idx -lt 1) { return }
    $key = $line.Substring(0, $idx).Trim()
    $val = $line.Substring($idx + 1).Trim()
    $map[$key] = $val
  }
  return $map
}

Require-File $ComposeFile "docker-compose.cluster-b.yml"
Require-File $EnvExample ".env.example"

if (-not (Test-Path $EnvFile)) {
  Copy-Item $EnvExample $EnvFile
  Write-Host ""
  Write-Host "Создан ops/cluster-b/.env из .env.example."
  Write-Host "Заполни минимум TELEGRAM_BOT_TOKEN и TELEGRAM_ALERT_CHAT_ID, затем запусти скрипт снова."
  Write-Host ""
  exit 1
}

try {
  docker compose version | Out-Null
} catch {
  throw "Команда 'docker compose' не найдена. Нужен Docker Desktop + Compose v2."
}

$envMap = Read-DotEnv $EnvFile

$autoparkPort = $envMap["AUTOPARK_PORT"]; if (-not $autoparkPort) { $autoparkPort = "8080" }
$notifyPort   = $envMap["NOTIFY_PORT"];   if (-not $notifyPort)   { $notifyPort   = "8082" }
$promPort     = $envMap["PROMETHEUS_PORT"]; if (-not $promPort)   { $promPort     = "9090" }
$grafanaPort  = $envMap["GRAFANA_PORT"];  if (-not $grafanaPort)  { $grafanaPort  = "3000" }
$token        = $envMap["INTERNAL_API_TOKEN"]; if (-not $token)    { $token = "dev-token" }

Push-Location $Root
try {
  docker compose --env-file .\.env -f .\docker-compose.cluster-b.yml up -d --build

  Write-Host ""
  Write-Host "Кластер B поднят."
  Write-Host "Autopark:    http://localhost:$autoparkPort"
  Write-Host "Notify:      http://localhost:$notifyPort"
  Write-Host "Prometheus:  http://localhost:$promPort"
  Write-Host "Grafana:     http://localhost:$grafanaPort"
  Write-Host ""

  Write-Host "Smoke-test: имитация webhook от Grafana -> notify-service (должно уйти в Telegram):"
  Write-Host "  Invoke-RestMethod -Method Post -Uri http://localhost:$notifyPort/api/grafana/webhook -Headers @{ Authorization = 'Bearer $token' } -ContentType 'application/json' -Body '{\"title\":\"smoke\",\"state\":\"alerting\",\"message\":\"cluster-b test\"}'"
  Write-Host ""
}
finally {
  Pop-Location
}
