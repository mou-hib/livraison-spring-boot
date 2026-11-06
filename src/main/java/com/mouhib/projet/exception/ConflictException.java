package com.mouhib.projet.exception;

// Thrown when data already exists (duplicate email, vehicle already used...) -> HTTP 409
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
