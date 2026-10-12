package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class CourseNotInMajorException extends DomainException {

    public CourseNotInMajorException() {
        super("Course must belong to student's major.");
    }
}
