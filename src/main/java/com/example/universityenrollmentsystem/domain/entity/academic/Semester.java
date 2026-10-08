package com.example.universityenrollmentsystem.domain.entity.academic;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Semester extends BaseEntity<Long> {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private final List<CourseOffering> courseOfferings = new ArrayList<>();

    public Semester(String title, LocalDate startDate, LocalDate endDate) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title cannot be null or empty");
            
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Dates cannot be null");
            
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");
            
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.isBlank())
            throw new IllegalArgumentException("Title cannot be null or empty");

        this.title = newTitle;
    }

    public void updateStartDate(LocalDate newStartDate) {
        if (newStartDate == null)
            throw new IllegalArgumentException("Start date cannot be null");

        if (this.endDate != null && this.endDate.isBefore(newStartDate))
            throw new IllegalArgumentException("Start date cannot be after end date");

        this.startDate = newStartDate;
    }

    public void updateEndDate(LocalDate newEndDate) {
        if (newEndDate == null)
            throw new IllegalArgumentException("End date cannot be null");

        if (this.startDate != null && newEndDate.isBefore(this.startDate))
            throw new IllegalArgumentException("End date cannot be before start date");

        this.endDate = newEndDate;
    }
    public List<CourseOffering> getCourseOfferings() {
        return Collections.unmodifiableList(courseOfferings);
    }

    public void addCourseOffering(CourseOffering courseOffering) {
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");
        if (this.courseOfferings.contains(courseOffering))
            throw new IllegalStateException("CourseOffering already added");
        this.courseOfferings.add(courseOffering);
    }

    public void removeCourseOffering(CourseOffering courseOffering) {
        if (courseOffering == null)
            throw new IllegalArgumentException("CourseOffering cannot be null");
        if (!this.courseOfferings.remove(courseOffering))
            throw new IllegalStateException("CourseOffering is not associated with this semester");
    }
}
