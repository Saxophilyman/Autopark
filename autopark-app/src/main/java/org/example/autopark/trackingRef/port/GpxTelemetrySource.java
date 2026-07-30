package org.example.autopark.trackingRef.port;



import org.example.autopark.trackingRef.model.TelemetrySeries;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Граница чтения GPS-телеметрии из GPX-документа.
 *
 * <p>Прикладной сценарий зависит от этого контракта, а не от DOM-парсера
 * или конкретной библиотеки работы с GPX.</p>
 */
public interface GpxTelemetrySource {

    TelemetrySeries read(
            InputStream source,
            LocalDateTime expectedStart,
            LocalDateTime expectedEnd
    );
}
