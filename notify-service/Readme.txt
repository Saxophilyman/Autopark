Как привязать Telegram-чат (чтобы алерты приходили лично вам)

Эта настройка делается один раз. После этого при запуске кластера B (Grafana → notify → Telegram) вы сразу будете видеть алерты в своём Telegram.

Что понадобится

Telegram-аккаунт

2 значения:

TELEGRAM_BOT_TOKEN

TELEGRAM_BOT_USERNAME

Далее вы получите:

TELEGRAM_ALERT_CHAT_ID

Как привязать Telegram-чат (чтобы алерты приходили вам)

Шаг 1. Создайте бота(2 минуты)
 - Откройте Telegram и найдите @BotFather
 - Напишите /newbot
 - Следуйте инструкциям
 - В конце BotFather даст вам TOKEN вида 123456:ABCDEF... и USERNAME бота (например my_autopark_bot)
 - Сохраните оба значения: TOKEN и USERNAME.

Шаг 2. Запустите notify-service в режиме B (одной командой)

В корне репозитория есть скрипт запуска режима B:

Windows: run-notify-B.ps1

Linux/macOS: run-notify-B.sh

Запустите скрипт и передайте token + username.

 Шаг 2. Узнайте свой chatId (это 10 секунд)
 - Запустите notify-service (режим B)
 - В Telegram найдите вашего бота по имени и нажмите Start
 - Напишите боту команду:
            /start
 - Бот ответит сообщением, где будет строка:
            Твой chatId: 123456789
 - Это число и есть ваш chatId. Скопируйте его.

 Шаг 3. Вставьте chatId и перезапустите notify

 Готово: теперь все алерты (из Grafana через notify) будут приходить в ваш личный чат.