package com.example.universityenrollmentsystem.service.exceptions.notfound;

public abstract class BaseNotFoundException extends RuntimeException {
    public BaseNotFoundException(String message) {
        super(message);
    }
}
