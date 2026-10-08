package com.example.universityenrollmentsystem.domain.entity.academic;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Major extends BaseEntity<Long> {
    private String name;
    private final List<Course> courses = new ArrayList<>();

    public Major(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be null or empty");
        
        this.name = name;
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Name cannot be null or empty");
        
        this.name = newName;
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public void addCourse(Course course) {
        if (course == null)
            throw new IllegalArgumentException("Course cannot be null");
            
        if (this.courses.contains(course))
            throw new IllegalStateException("Course is already added to this major");
            
        this.courses.add(course);
    }

    public void removeCourse(Course course) {
        if (course == null)
            throw new IllegalArgumentException("Course cannot be null");

        if (!this.courses.remove(course))
            throw new IllegalStateException("Course is not part of this major");
    }
}
