package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class CourseNotFoundException extends BaseNotFoundException {
    public CourseNotFoundException(Long id) {
        super("Course with ID " + id + " not found.");
    }
}
