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

    public void assignGrade(Double grade) {
        if (grade == null)
            throw new IllegalArgumentException("Grade cannot be null");

        if (grade < 0.0 || grade > 20.0)
            throw new IllegalArgumentException("Grade must be between 0 and 20");

        if (java.math.BigDecimal.valueOf(grade).stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Grade can have at most 2 decimal places");

        if (this.status != CourseStatus.IN_PROGRESS)
            throw new IllegalStateException("Can only assign grade to in-progress courses");

        this.grade = grade;
        
        if (grade >= 10.0)
            this.status = CourseStatus.PASSED;
        else
            this.status = CourseStatus.FAILED;
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
