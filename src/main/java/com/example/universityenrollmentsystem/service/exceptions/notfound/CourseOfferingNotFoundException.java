package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class CourseOfferingNotFoundException extends BaseNotFoundException {
    public CourseOfferingNotFoundException(Long id) {
        super("CourseOffering with ID " + id + " not found.");
    }
}
