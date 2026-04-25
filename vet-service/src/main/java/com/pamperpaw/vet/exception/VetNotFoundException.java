package com.pamperpaw.vet.exception;

public class VetNotFoundException extends RuntimeException {
    public VetNotFoundException(String message) {
        super(message);
    }
}