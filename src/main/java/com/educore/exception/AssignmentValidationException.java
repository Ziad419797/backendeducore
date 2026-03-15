package com.educore.exception;

public class AssignmentValidationException extends RuntimeException {

    public AssignmentValidationException(String message) {
        super(message);
    }

    public AssignmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    }