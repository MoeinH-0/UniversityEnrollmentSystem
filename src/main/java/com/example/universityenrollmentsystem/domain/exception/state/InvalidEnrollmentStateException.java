package com.example.universityenrollmentsystem.domain.exception.state;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class InvalidEnrollmentStateException extends DomainException {

    public InvalidEnrollmentStateException(String message) {
        super(message);
    }
}
