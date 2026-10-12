package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class CrossSemesterEnrollmentException extends DomainException {

    public CrossSemesterEnrollmentException() {
        super("All requested course offerings must belong to the same semester.");
    }
}
