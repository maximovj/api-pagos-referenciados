package com.github.maximovj.pagos_referenciados.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.github.maximovj.pagos_referenciados.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja excepciones de validación @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse response = ApiResponse.builder()
                .response_code(HttpStatus.BAD_REQUEST.value())
                .response_message("Error de validación")
                .data(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Maneja excepciones generales
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", ex.getMessage());

        ApiResponse response = ApiResponse.builder()
                .response_code(HttpStatus.BAD_REQUEST.value())
                .response_message("Error de negocio")
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Maneja cualquier otra excepción no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAllExceptions(Exception ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", ex.getMessage());

        ApiResponse response = ApiResponse.builder()
                .response_code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .response_message("Error interno del servidor")
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}