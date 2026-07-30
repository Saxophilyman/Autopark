package org.example.autopark.exportAndImport.ref.format;

import lombok.Getter;
import org.example.autopark.exportAndImport.ref.format.exception.UnsupportedDataFormatException;
import org.springframework.http.MediaType;

import java.util.Locale;

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

    public static DataFormat fromRequestParam(String format) {
        if (format == null || format.isBlank()) {
            return JSON;
        }

        String normalized = format.strip();
        for (DataFormat value : values()) {
            if (value.name().equalsIgnoreCase(normalized) || value.extension.equalsIgnoreCase(normalized)) {
                return value;
            }
        }

        throw new UnsupportedDataFormatException("Поддерживаются только JSON и CSV");
    }

    public static DataFormat fromFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new UnsupportedDataFormatException("Файл должен иметь имя и расширение");
        }

        String normalized = filename.strip().toLowerCase(Locale.ROOT);
        for (DataFormat value : values()) {
            if (normalized.endsWith("." + value.extension)) {
                return value;
            }
        }

        throw new UnsupportedDataFormatException("Поддерживаются только файлы JSON и CSV");
    }
}