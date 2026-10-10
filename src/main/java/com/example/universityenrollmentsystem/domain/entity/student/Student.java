package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanStrategy;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "students")

public class Student extends BaseEntity<Long> {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String studentNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Enumerated(EnumType.STRING)
    private EnrollmentPlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnrollmentRecord> enrollments = new ArrayList<>();

    public Student(String fullName, String studentNumber, Major major, EnrollmentPlanType planType) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("FullName cannot be null or empty");

        if (studentNumber == null || studentNumber.isBlank())
            throw new IllegalArgumentException("StudentNumber cannot be null or empty");

        if (major == null)
            throw new IllegalArgumentException("Major cannot be null");

        if (planType == null)
            throw new IllegalArgumentException("PlanType cannot be null");

        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.major = major;
        this.planType = planType;
    }

    protected Student(){}

    public void updateContactInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
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

    public void updatePlanType(EnrollmentPlanType newPlanType) {
        if (newPlanType == null)
            throw new IllegalArgumentException("PlanType cannot be null");
            
        this.planType = newPlanType;
    }

    public List<EnrollmentRecord> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public void enroll(List<CourseOffering> courseOfferings, EnrollmentPlanStrategy strategy) {
        if (courseOfferings == null || courseOfferings.isEmpty())
            throw new IllegalArgumentException("CourseOfferings cannot be null or empty");

        if (strategy == null)
            throw new IllegalArgumentException("Strategy cannot be null");

        strategy.validate(this, courseOfferings);

        for (CourseOffering courseOffering : courseOfferings) {
            boolean alreadyEnrolledOrPassed = enrollments.stream()
                    .anyMatch(r -> r.getCourseOffering().equals(courseOffering) &&
                            (r.getStatus() == CourseStatus.IN_PROGRESS || r.getStatus() == CourseStatus.PASSED));

            if (alreadyEnrolledOrPassed)
                throw new IllegalStateException("Student is already enrolled in or has passed this course");

            EnrollmentRecord record = new EnrollmentRecord(this, courseOffering);
            this.enrollments.add(record);
        }
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
