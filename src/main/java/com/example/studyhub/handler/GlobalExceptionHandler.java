package com.example.studyhub.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura erros de validação (@Valid, @NotNull, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        // Retorna 400 Bad Request com todos os erros de validação no corpo
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Captura exceções lançadas por Service
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getReason());
        // Retorna o status HTTP definido na exceção (ex: 401, 403, 404)
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // Tratar o JSON malformado (HttpMessageNotReadableException)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParsingError(HttpMessageNotReadableException ex) {
        Map<String, String> errorResponse = Collections.singletonMap("error", "Corpo da requisição inválido ou JSON malformado.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}