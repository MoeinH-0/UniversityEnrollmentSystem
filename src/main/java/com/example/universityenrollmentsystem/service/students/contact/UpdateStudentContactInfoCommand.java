package com.example.universityenrollmentsystem.service.students.contact;

public record UpdateStudentContactInfoCommand(Long studentId, String email, String phoneNumber) {}
