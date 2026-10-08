package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import lombok.Getter;

@Getter
public class CourseOffering extends BaseEntity<Long> {
    private final Course course;
    private final Semester semester;
    private String professorName;
    private String section;

    public CourseOffering(Course course, Semester semester, String professorName, String section) {
        if (course == null)
            throw new IllegalArgumentException("Course cannot be null");
            
        if (semester == null)
            throw new IllegalArgumentException("Semester cannot be null");
            
        this.course = course;
        this.semester = semester;
        this.professorName = professorName;
        this.section = section;
    }

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
}
