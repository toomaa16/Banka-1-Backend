package app.exception;

import app.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
