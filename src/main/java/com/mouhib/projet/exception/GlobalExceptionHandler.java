package com.mouhib.projet.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// One place that catches the errors of ALL controllers
// and turns them into a clean JSON response with the right HTTP code.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - id not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 400 - business rule broken
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 409 - duplicate data
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // 400 - @Valid failed (@NotBlank, @Email, @Positive...) -> also lists each wrong field
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> champs = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            champs.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return erreur(HttpStatus.BAD_REQUEST, "Donnees invalides", champs);
    }

    // 400 - JSON badly written, or wrong enum value (e.g. "type": "AVION")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "JSON invalide ou valeur incorrecte", null);
    }

    // 400 - wrong value in the URL (e.g. /api/commandes/statut/XYZ or /api/clients/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "Valeur invalide pour le parametre : " + ex.getName(), null);
    }

    // 409 - safety net: the database refused the operation (unique or foreign key)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return erreur(HttpStatus.CONFLICT, "Operation refusee par la base de donnees (donnee en double ou liee)", null);
    }

    // Builds the JSON body: { timestamp, status, message, [erreurs] }
    private ResponseEntity<Map<String, Object>> erreur(HttpStatus status, String message, Map<String, String> champs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        if (champs != null) {
            body.put("erreurs", champs);
        }
        return ResponseEntity.status(status).body(body);
    }
}
