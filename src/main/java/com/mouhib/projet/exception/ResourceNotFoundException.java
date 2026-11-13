package com.mouhib.projet.exception;

// Thrown when an id does not exist in the database -> HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    // e.g. new ResourceNotFoundException("Client", 99) -> "Client introuvable avec l'id 99"
    public ResourceNotFoundException(String entite, Long id) {
        super(entite + " introuvable avec l'id " + id);
    }
}
