package com.example.universityenrollmentsystem.service.exceptions.conflict;

public class StudentAlreadyExistsException extends BaseConflictException {
    public StudentAlreadyExistsException(String studentNumber) {
        super("Student with number " + studentNumber + " already exists.");
    }
}
