package org.example.autopark.trackingRef.adapter.gpx;


import org.example.autopark.trackingRef.model.TelemetryPoint;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.port.GpxTelemetrySource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Адаптер GPX-формата: преобразует внешний файл в нейтральную телеметрию.
 * Ничего не знает об автомобилях, поездках, репозиториях и транзакциях.
 */
@Component
public class GpxTelemetryReader implements GpxTelemetrySource {

    @Override
    public TelemetrySeries read(
            InputStream source,
            LocalDateTime expectedStart,
            LocalDateTime expectedEnd
    ) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "Поток GPX-файла обязателен"
            );
        }

        List<TelemetryPoint> parsed;
        try {
            parsed = GpxParser.parse(
                    source,
                    expectedStart,
                    expectedEnd
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "Ошибка чтения GPX-файла: "
                            + exception.getMessage(),
                    exception
            );
        }

        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(
                    "GPX-файл не содержит допустимых точек маршрута"
            );
        }

        return new TelemetrySeries(parsed);
    }
}
