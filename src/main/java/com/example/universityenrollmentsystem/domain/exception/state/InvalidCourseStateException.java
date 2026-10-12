package com.example.universityenrollmentsystem.domain.exception.state;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class InvalidCourseStateException extends DomainException {

    public InvalidCourseStateException(String message) {
        super(message);
    }
}
