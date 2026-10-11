package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class CourseAlreadyExistsException extends RuntimeException {
    public CourseAlreadyExistsException(String courseCode) {
        super("Course with code " + courseCode + " already exists.");
    }
}
