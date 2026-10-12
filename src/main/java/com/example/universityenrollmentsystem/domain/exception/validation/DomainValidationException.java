package com.example.universityenrollmentsystem.domain.exception.validation;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        super(message);
    }
}
