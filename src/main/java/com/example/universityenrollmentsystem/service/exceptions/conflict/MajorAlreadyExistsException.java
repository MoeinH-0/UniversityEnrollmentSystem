package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class MajorAlreadyExistsException extends RuntimeException {
    public MajorAlreadyExistsException(String name) {
        super("Major with name '" + name + "' already exists.");
    }
}
