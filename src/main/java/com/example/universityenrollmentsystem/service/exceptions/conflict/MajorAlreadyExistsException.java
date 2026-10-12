package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class MajorAlreadyExistsException extends BaseConflictException {
    public MajorAlreadyExistsException(String name) {
        super("Major with name '" + name + "' already exists.");
    }
}
