package com.mouhib.projet.exception;

// Thrown when a business rule is broken (not enough stock, wrong order status...) -> HTTP 400
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
