package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "course_offerings")
public class CourseOffering extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false, length = 100)
    private String professorName;

    @Column(nullable = false, length = 30)
    private String section;

    @Column(nullable = false)
    private boolean guestAllowed;

    @Column(nullable = false)
    private int capacity;

    @Column(length = 100)
    private String classTime;

    @Column
    private LocalDate examDate;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public CourseOffering(Course course, Semester semester, String professorName,
                          String section, boolean guestAllowed, int capacity,
                          String classTime, LocalDate examDate) {
        if (course == null)
            throw new IllegalArgumentException("Course cannot be null");

        if (semester == null)
            throw new IllegalArgumentException("Semester cannot be null");

        if (professorName == null || professorName.isBlank())
            throw new IllegalArgumentException("ProfessorName cannot be null or empty");

        if (section == null || section.isBlank())
            throw new IllegalArgumentException("Section cannot be null or empty");

        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than zero");

        if (examDate != null && (examDate.isBefore(semester.getStartDate()) || examDate.isAfter(semester.getEndDate())))
            throw new IllegalArgumentException("Exam date must be within the semester dates");

        this.course = course;
        this.semester = semester;
        this.professorName = professorName;
        this.section = section;
        this.guestAllowed = guestAllowed;
        this.capacity = capacity;
        this.classTime = classTime;
        this.examDate = examDate;
    }

    protected CourseOffering() {}

    public void changeProfessor(String newProfessorName) {
        if (newProfessorName == null || newProfessorName.isBlank())
            throw new IllegalArgumentException("ProfessorName cannot be null or empty");

        this.professorName = newProfessorName;
    }

    public void updateSection(String newSection) {
        if (newSection == null || newSection.isBlank())
            throw new IllegalArgumentException("Section cannot be null or empty");

        this.section = newSection;
    }

    public void updateCapacity(int newCapacity) {
        if (newCapacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than zero");

        this.capacity = newCapacity;
    }

    public void updateClassTime(String newClassTime) {
        this.classTime = newClassTime;
    }

    public void updateExamDate(LocalDate newExamDate) {
        if (newExamDate != null && (newExamDate.isBefore(this.semester.getStartDate()) || newExamDate.isAfter(this.semester.getEndDate())))
            throw new IllegalArgumentException("Exam date must be within the semester dates");

        this.examDate = newExamDate;
    }
}
