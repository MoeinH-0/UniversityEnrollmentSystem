package com.example.universityenrollmentsystem.domain.exception.state;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class InvalidMajorStateException extends DomainException {

    public InvalidMajorStateException(String message) {
        super(message);
    }
}
