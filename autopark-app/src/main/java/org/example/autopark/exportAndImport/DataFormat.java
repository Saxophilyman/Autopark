package org.example.autopark.exportAndImport;

import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public enum DataFormat {
    JSON("json", MediaType.APPLICATION_JSON_VALUE),
    CSV("csv", "text/csv");

    private final String extension;
    private final String contentType;

    DataFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    // определяем формат из request param
    // по сути для export-сценария
    public static DataFormat fromRequestParam(String format) {
        if (format == null || format.isBlank()) {
            return JSON;
        }

        for (DataFormat value : values()) {
            if (value.name().equalsIgnoreCase(format) || value.extension.equalsIgnoreCase(format)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Поддерживаются только JSON и CSV");
    }
    //определяем формат из имени загруженного файла и его расширения
    //по сути для import-сценария
    public static DataFormat fromFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Файл без имени");
        }

        String lower = filename.toLowerCase();

        if (lower.endsWith(".json")) {
            return JSON;
        }
        if (lower.endsWith(".csv")) {
            return CSV;
        }

        throw new IllegalArgumentException("Поддерживаются только JSON и CSV");
    }
}