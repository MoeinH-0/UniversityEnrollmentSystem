package com.example.universityenrollmentsystem.service.exceptions.notfound;

public class EnrollmentRecordNotFoundException extends BaseNotFoundException {
    public EnrollmentRecordNotFoundException(Long studentId, Long courseOfferingId) {
        super("Enrollment record for student " + studentId + " in course offering " + courseOfferingId + " not found.");
    }
}
