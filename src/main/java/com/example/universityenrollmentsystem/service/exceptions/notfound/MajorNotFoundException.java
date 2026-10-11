package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class MajorNotFoundException extends BaseNotFoundException {
    public MajorNotFoundException(Long id) {
        super("Major with ID " + id + " not found.");
    }
}
