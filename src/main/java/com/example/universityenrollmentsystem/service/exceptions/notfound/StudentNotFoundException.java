package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class StudentNotFoundException extends BaseNotFoundException {
    public StudentNotFoundException(Long id) {
        super("Student with ID " + id + " not found.");
    }
}
