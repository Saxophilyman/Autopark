Установка Docker и Docker Compose на сервер

# 1. Обновляем пакеты
apt update && apt upgrade -y

# 2. Устанавливаем необходимые утилиты
apt install -y ca-certificates curl gnupg lsb-release

# 3. Создаём папку для ключей безопасности Docker
mkdir -p /etc/apt/keyrings

# 4. Скачиваем и сохраняем официальный ключ Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 5. Добавляем официальный репозиторий Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 6. Обновляем список пакетов
apt update

# 7. Устанавливаем Docker и Compose
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin


После успешного выполнения всех команд проверь установку:
docker --version
docker compose version

Ожидаемый результат:
Docker version XX.XX.X, build XXXXX
Docker Compose version v2.X.X
