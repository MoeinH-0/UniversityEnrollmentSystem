package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "enrollment_records")
public class EnrollmentRecord extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Column
    private Double grade;

    public EnrollmentRecord(Student student, CourseOffering courseOffering) {
        if (student == null)
            throw new IllegalArgumentException("Student cannot be null");
            
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");
            
        this.student = student;
        this.courseOffering = courseOffering;
        this.status = CourseStatus.IN_PROGRESS;
    }

    protected EnrollmentRecord(){}

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
