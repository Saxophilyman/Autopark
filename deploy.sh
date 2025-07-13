#!/bin/bash

# === НАСТРОЙКИ ===
HOST="root@89.169.47.147"                      # IP твоего VPS
PROJECT_DIR="/opt/Autopark"                    # Путь до проекта на сервере
BRANCH="develop"                                 # Название ветки (или master) ПОКА ЧТО DEVELOP
REPO_URL="git@github.com:Saxophilyman/Autopark.git"  # SSH-ссылка на репозиторий

echo "[1] Подключение к $HOST и деплой в $PROJECT_DIR"

ssh "$HOST" << EOF
  set -e  # Остановить при любой ошибке

  if [ ! -d "$PROJECT_DIR" ]; then
    echo "Клонируем проект"
    git clone $REPO_URL $PROJECT_DIR
  fi

  cd $PROJECT_DIR

  echo "[2] Обновляем код из Git"
  git pull --rebase origin $BRANCH

  echo "[3] Перезапускаем контейнеры"
  docker compose down
  docker compose up -d --build

  echo "[✔] Деплой завершён"
EOF
