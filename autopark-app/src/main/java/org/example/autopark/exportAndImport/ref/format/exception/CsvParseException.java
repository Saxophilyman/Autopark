package org.example.autopark.exportAndImport.ref.format.exception;

public class CsvParseException extends RuntimeException {
    public CsvParseException(String message) {
        super(message);
    }

    public CsvParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

