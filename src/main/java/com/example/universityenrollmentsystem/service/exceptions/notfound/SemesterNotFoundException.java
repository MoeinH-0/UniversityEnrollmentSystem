package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class SemesterNotFoundException extends BaseNotFoundException {
    public SemesterNotFoundException(Long id) {
        super("Semester with ID " + id + " not found.");
    }
}
