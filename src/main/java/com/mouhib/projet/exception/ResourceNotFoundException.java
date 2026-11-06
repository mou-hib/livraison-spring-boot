package com.mouhib.projet.exception;

// Thrown when an id does not exist in the database -> HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
