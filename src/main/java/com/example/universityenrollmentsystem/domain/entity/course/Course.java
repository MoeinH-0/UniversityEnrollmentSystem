package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Course extends BaseEntity<Long> {
    private String name;
    private String courseCode;
    private final int credits;
    private final List<Major> majors = new ArrayList<>();

    public Course(String name, String courseCode, int credits) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be null or empty");
            
        if (courseCode == null || courseCode.isBlank())
            throw new IllegalArgumentException("CourseCode cannot be null or empty");
            
        if (credits <= 0)
            throw new IllegalArgumentException("Credits must be greater than zero");
            
        this.name = name;
        this.courseCode = courseCode;
        this.credits = credits;
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

    public void addMajor(Major major) {
        if (major == null)
            throw new IllegalArgumentException("Major cannot be null");
        if (this.majors.contains(major))
            throw new IllegalStateException("Major already added");
        this.majors.add(major);
    }

    public void removeMajor(Major major) {
        if (major == null)
            throw new IllegalArgumentException("Major cannot be null");
        if (!this.majors.remove(major))
            throw new IllegalStateException("Major is not associated with this course");
    }
}
