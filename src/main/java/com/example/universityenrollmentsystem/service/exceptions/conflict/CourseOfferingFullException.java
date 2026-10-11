package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class CourseOfferingFullException extends RuntimeException {
    public CourseOfferingFullException(Long offeringId) {
        super("Course offering with ID " + offeringId + " has reached maximum capacity.");
    }
}
