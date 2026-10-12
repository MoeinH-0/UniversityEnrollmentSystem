package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class PrerequisiteNotMetException extends DomainException {

    public PrerequisiteNotMetException(String courseName) {
        super("Prerequisite not passed for course: " + courseName);
    }
}
