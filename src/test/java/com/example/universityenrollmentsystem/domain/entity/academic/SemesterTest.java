package com.example.universityenrollmentsystem.domain.entity.academic;

import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemesterTest {

    @Test
    @DisplayName("Should initialize semester successfully with valid values")
    void shouldInitializeSemesterSuccessfully() {
        LocalDate start = LocalDate.of(2026, 9, 23);
        LocalDate end = LocalDate.of(2027, 1, 20);

        Semester semester = new Semester("Fall 2026", start, end);

        assertThat(semester.getTitle()).isEqualTo("Fall 2026");
        assertThat(semester.getStartDate()).isEqualTo(start);
        assertThat(semester.getEndDate()).isEqualTo(end);
        assertThat(semester.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when title is null or blank")
    void shouldThrowExceptionWhenTitleNullOrBlank() {
        LocalDate start = LocalDate.of(2026, 9, 23);
        LocalDate end = LocalDate.of(2027, 1, 20);

        assertThatThrownBy(() -> new Semester(null, start, end))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Semester("   ", start, end))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when start date or end date is null")
    void shouldThrowExceptionWhenDatesNull() {
        LocalDate date = LocalDate.of(2026, 9, 23);

        assertThatThrownBy(() -> new Semester("Fall", null, date))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new Semester("Fall", date, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        LocalDate start = LocalDate.of(2026, 9, 23);
        LocalDate end = LocalDate.of(2026, 9, 20);

        assertThatThrownBy(() -> new Semester("Fall", start, end))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    @DisplayName("Should reschedule semester dates successfully")
    void shouldRescheduleSemester() {
        LocalDate start = LocalDate.of(2026, 9, 23);
        LocalDate end = LocalDate.of(2027, 1, 20);
        Semester semester = new Semester("Fall 2026", start, end);

        LocalDate newStart = LocalDate.of(2026, 10, 1);
        LocalDate newEnd = LocalDate.of(2027, 1, 30);
        semester.reschedule(newStart, newEnd);

        assertThat(semester.getStartDate()).isEqualTo(newStart);
        assertThat(semester.getEndDate()).isEqualTo(newEnd);
    }

    @Test
    @DisplayName("Should throw exception when rescheduling with end date before start date")
    void shouldThrowExceptionWhenReschedulingInvalidDates() {
        LocalDate start = LocalDate.of(2026, 9, 23);
        LocalDate end = LocalDate.of(2027, 1, 20);
        Semester semester = new Semester("Fall 2026", start, end);

        LocalDate newStart = LocalDate.of(2027, 2, 1);
        LocalDate newEnd = LocalDate.of(2027, 1, 1);

        assertThatThrownBy(() -> semester.reschedule(newStart, newEnd))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should mark semester as deleted")
    void shouldMarkAsDeleted() {
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));

        semester.markAsDeleted();

        assertThat(semester.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should successfully initialize single day semester when startDate equals endDate")
    void shouldInitializeSingleDaySemesterSuccessfully() {
        LocalDate sameDate = LocalDate.of(2026, 9, 23);

        Semester semester = new Semester("Workshop Semester", sameDate, sameDate);

        assertThat(semester.getStartDate()).isEqualTo(sameDate);
        assertThat(semester.getEndDate()).isEqualTo(sameDate);
    }
}
