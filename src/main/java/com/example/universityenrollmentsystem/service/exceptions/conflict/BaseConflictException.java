package com.example.universityenrollmentsystem.service.exceptions.conflict;

public abstract class BaseConflictException extends RuntimeException {

    public BaseConflictException(String message) {
        super(message);
    }
}
