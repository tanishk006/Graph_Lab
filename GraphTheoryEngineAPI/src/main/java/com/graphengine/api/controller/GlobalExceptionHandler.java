package com.graphengine.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Triggered when @Valid fails on the request body (e.g. missing fields, wrong type value)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    // Triggered when a vertex/edge index doesn't fit the declared vertexCount
    // (e.g. a matrix graph built with vertexCount=3 but an edge references vertex 5)
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseEntity<Map<String, String>> handleOutOfBounds(ArrayIndexOutOfBoundsException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "A vertex referenced in your edges is outside the declared vertexCount.")
        );
    }
}
