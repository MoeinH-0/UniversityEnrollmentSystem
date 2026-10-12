package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class DuplicateEnrollmentException extends DomainException {

    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}
