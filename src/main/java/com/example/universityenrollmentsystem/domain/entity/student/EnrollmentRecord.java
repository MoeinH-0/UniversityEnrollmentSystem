package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import lombok.Getter;

@Getter
public class EnrollmentRecord extends BaseEntity<Long> {
    private final Student student;
    private final CourseOffering courseOffering;
    private CourseStatus status;

    public EnrollmentRecord(Student student, CourseOffering courseOffering) {
        if (student == null)
            throw new IllegalArgumentException("Student cannot be null");
            
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");
            
        this.student = student;
        this.courseOffering = courseOffering;
        this.status = CourseStatus.IN_PROGRESS;
    }

    public void markAsPassed() {
        if (this.status != CourseStatus.IN_PROGRESS)
            throw new IllegalStateException("Only in-progress courses can be marked as passed");
            
        this.status = CourseStatus.PASSED;
    }

    public void markAsFailed() {
        if (this.status != CourseStatus.IN_PROGRESS)
            throw new IllegalStateException("Only in-progress courses can be marked as failed");
            
        this.status = CourseStatus.FAILED;
    }
}
