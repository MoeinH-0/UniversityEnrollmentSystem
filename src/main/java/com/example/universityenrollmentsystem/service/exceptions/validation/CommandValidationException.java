package com.example.universityenrollmentsystem.service.exceptions.validation;

public class CommandValidationException extends RuntimeException {
    public CommandValidationException(String message) {
        super(message);
    }
}
