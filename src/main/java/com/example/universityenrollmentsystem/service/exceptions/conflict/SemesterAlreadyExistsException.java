package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class SemesterAlreadyExistsException extends BaseConflictException {
    public SemesterAlreadyExistsException(String title) {
        super("Semester with title '" + title + "' already exists.");
    }
}
