package com.example.universityenrollmentsystem.domain.entity.academic;

import com.example.universityenrollmentsystem.domain.entity.BaseEntity;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "semesters")
public class Semester extends BaseEntity<Long> {

    @Column(nullable = false, length = 100, unique = true)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public Semester(String title, LocalDate startDate, LocalDate endDate) {
        if (title == null || title.isBlank())
            throw new DomainValidationException("Title cannot be null or empty");

        if (startDate == null || endDate == null)
            throw new DomainValidationException("Dates cannot be null");

        if (endDate.isBefore(startDate))
            throw new DomainValidationException("End date cannot be before start date");

        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    protected Semester() {}

    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.isBlank())
            throw new DomainValidationException("Title cannot be null or empty");

        this.title = newTitle;
    }

    public void reschedule(LocalDate newStartDate, LocalDate newEndDate) {
        if (newStartDate == null || newEndDate == null)
            throw new DomainValidationException("Dates cannot be null");

        if (newEndDate.isBefore(newStartDate))
            throw new DomainValidationException("End date cannot be before start date");

        this.startDate = newStartDate;
        this.endDate = newEndDate;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
