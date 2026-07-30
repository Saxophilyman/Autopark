package org.example.autopark.exportAndImport.ref.entry.web.error;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.exception.ResourceNotFoundException;
import org.example.autopark.exception.VehicleNotFoundException;
import org.example.autopark.exportAndImport.ref.format.exception.CsvParseException;
import org.example.autopark.exportAndImport.ref.entry.batch.BatchLaunchException;

import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.example.autopark.exportAndImport.ref.format.exception.UnsupportedDataFormatException;
import org.example.autopark.exportAndImport.ref.format.exception.UnsupportedExportCombinationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "org.example.autopark.exportAndImport")
public class ExchangeExceptionHandler {

    @ExceptionHandler({UnsupportedDataFormatException.class, UnsupportedExportCombinationException.class, IllegalArgumentException.class})
    public ResponseEntity<ExchangeErrorResponse> handleBadRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(new ExchangeErrorResponse("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler({InvalidExchangeDocumentException.class, CsvParseException.class})
    public ResponseEntity<ExchangeErrorResponse> handleInvalidDocument(RuntimeException exception) {
        return ResponseEntity.unprocessableEntity().body(new ExchangeErrorResponse("INVALID_DOCUMENT", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExchangeErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExchangeErrorResponse("ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler({VehicleNotFoundException.class, ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ExchangeErrorResponse> handleNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExchangeErrorResponse("NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(BatchLaunchException.class)
    public ResponseEntity<ExchangeErrorResponse> handleBatchLaunch(BatchLaunchException exception) {
        log.error("Batch launch error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExchangeErrorResponse("BATCH_LAUNCH_ERROR", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExchangeErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected vehicle exchange error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExchangeErrorResponse("INTERNAL_ERROR", "Внутренняя ошибка импорта или экспорта"));
    }
}
