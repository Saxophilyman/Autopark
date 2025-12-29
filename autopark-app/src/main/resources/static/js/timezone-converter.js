document.addEventListener('DOMContentLoaded', function () {
    let dateElements = document.querySelectorAll('.convert-timezone');

    dateElements.forEach(el => {
        let utcTime = el.dataset.utc; // Берём чистое UTC время
        // let enterpriseTimeZone = el.dataset.enterpriseTz;

        if (!utcTime) {
            el.innerText = "Ошибка времени";
            return;
        }

        let utcDate = new Date(Date.parse(utcTime)); // Читаем UTC

        let clientTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

        // Переводим в таймзону пользователя
        // Показываем пользователю его локальное время
        el.innerText = utcDate.toLocaleString("ru-RU", {timeZone: clientTimeZone});
    });
});
