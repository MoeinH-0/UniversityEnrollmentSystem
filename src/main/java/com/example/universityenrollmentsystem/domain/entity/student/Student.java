package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Student extends BaseEntity<Long> {
    private String fullName;
    private final String studentNumber;
    private Major major;
    private final List<EnrollmentRecord> enrollments = new ArrayList<>();

    public Student(String fullName, String studentNumber, Major major) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("FullName cannot be null or empty");
            
        if (studentNumber == null || studentNumber.isBlank())
            throw new IllegalArgumentException("StudentNumber cannot be null or empty");
            
        if (major == null)
            throw new IllegalArgumentException("Major cannot be null");
            
        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.major = major;
    }

    public void updateFullName(String newFullName) {
        if (newFullName == null || newFullName.isBlank())
            throw new IllegalArgumentException("FullName cannot be null or empty");
            
        this.fullName = newFullName;
    }

    public void updateMajor(Major newMajor) {
        if (newMajor == null)
            throw new IllegalArgumentException("Major cannot be null");

        this.major = newMajor;
    }

    public void enroll(CourseOffering courseOffering) {
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");

        EnrollmentRecord record = new EnrollmentRecord(this, courseOffering);
        this.enrollments.add(record);
    }

    public void drop(CourseOffering courseOffering) {
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");

        boolean removed = this.enrollments.removeIf(record -> 
            record.getCourseOffering().equals(courseOffering) && 
            record.getStatus() == CourseStatus.IN_PROGRESS
        );

        if (!removed)
            throw new IllegalStateException("Cannot drop course that is not in progress or doesn't exist");
    }
}
