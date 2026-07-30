package org.example.autopark.exportAndImport.ref.format.exception;

public class InvalidExchangeDocumentException extends RuntimeException {
    public InvalidExchangeDocumentException(String message) {
        super(message);
    }

    public InvalidExchangeDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
