package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
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
            throw new DomainValidationException("Student cannot be null");
            
        if (courseOffering == null)
            throw new DomainValidationException("CourseOffering cannot be null");
            
        this.student = student;
        this.courseOffering = courseOffering;
        this.status = CourseStatus.IN_PROGRESS;
    }

    protected EnrollmentRecord(){}

    public static final double MIN_GRADE = 0.0;
    public static final double MAX_GRADE = 20.0;
    public static final double PASSING_GRADE = 10.0;
    public static final int MAX_GRADE_DECIMALS = 2;

    public void assignGrade(Double grade) {
        if (grade == null)
            throw new DomainValidationException("Grade cannot be null");

        if (grade < MIN_GRADE || grade > MAX_GRADE)
            throw new DomainValidationException("Grade must be between " + MIN_GRADE + " and " + MAX_GRADE);

        if (java.math.BigDecimal.valueOf(grade).stripTrailingZeros().scale() > MAX_GRADE_DECIMALS)
            throw new DomainValidationException("Grade can have at most " + MAX_GRADE_DECIMALS + " decimal places");

        if (this.status != CourseStatus.IN_PROGRESS)
            throw new InvalidEnrollmentStateException("Can only assign grade to in-progress courses");

        this.grade = grade;
        
        if (grade >= PASSING_GRADE)
            this.status = CourseStatus.PASSED;
        else
            this.status = CourseStatus.FAILED;
    }

    public void markAsPassed() {
        if (this.status != CourseStatus.IN_PROGRESS)
            throw new InvalidEnrollmentStateException("Only in-progress courses can be marked as passed");

        this.status = CourseStatus.PASSED;
    }

    public void markAsFailed() {
        if (this.status != CourseStatus.IN_PROGRESS)
            throw new InvalidEnrollmentStateException("Only in-progress courses can be marked as failed");

        this.status = CourseStatus.FAILED;
    }
}
