package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "courses")
public class Course extends BaseEntity<Long> {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30, unique = true)
    private String courseCode;

    @Column(nullable = false)
    @Max(value = MAX_CREDITS)
    @Min(value = MIN_CREDITS)
    private int credits;

    @ManyToMany(mappedBy = "courses")
    private List<Major> majors = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private List<Course> prerequisites = new ArrayList<>();

    public static final int MIN_CREDITS = 1;
    public static final int MAX_CREDITS = 4;

    public Course(String name, String courseCode, int credits) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be null or empty");

        if (courseCode == null || courseCode.isBlank())
            throw new IllegalArgumentException("CourseCode cannot be null or empty");

        if (credits < MIN_CREDITS || credits > MAX_CREDITS)
            throw new IllegalArgumentException(String.format(
                    "Credits must be between %d and %d", MIN_CREDITS, MAX_CREDITS));

        this.name = name;
        this.courseCode = courseCode;
        this.credits = credits;
    }

    protected Course() {

    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Name cannot be null or empty");

        this.name = newName;
    }

    public void updateCourseCode(String newCourseCode) {
        if (newCourseCode == null || newCourseCode.isBlank())
            throw new IllegalArgumentException("CourseCode cannot be null or empty");

        this.courseCode = newCourseCode;
    }

    public List<Major> getMajors() {
        return Collections.unmodifiableList(majors);
    }

    public List<Course> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    public void addPrerequisite(Course course) {
        if (course == null)
            throw new IllegalArgumentException("Prerequisite cannot be null");

        if (this.equals(course))
            throw new IllegalStateException("A course cannot be a prerequisite of itself");

        if (this.prerequisites.contains(course))
            throw new IllegalStateException("Prerequisite already added");

        this.prerequisites.add(course);
    }

    public void removePrerequisite(Course course) {
        if (course == null)
            throw new IllegalArgumentException("Prerequisite cannot be null");

        if (!this.prerequisites.remove(course))
            throw new IllegalStateException("Course is not a prerequisite");
    }
}
