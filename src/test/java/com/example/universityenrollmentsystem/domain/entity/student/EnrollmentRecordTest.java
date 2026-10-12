package com.example.universityenrollmentsystem.domain.entity.student;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentRecordTest {

    private static final double PASSING_GRADE = 10.0;
    private static final double FAILING_GRADE = 9.99;
    private static final double MIN_GRADE = 0.0;
    private static final double MAX_GRADE = 20.0;
    private static final double BELOW_MIN_GRADE = -0.1;
    private static final double ABOVE_MAX_GRADE = 20.1;
    private static final double THREE_DECIMAL_GRADE = 15.123;
    private static final double TWO_DECIMAL_GRADE = 15.25;

    private Student student;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        Major major = new Major("Software Engineering");
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        Course course = new Course("Databases", "CS204", 3);
        major.addCourse(course);

        offering = new CourseOffering(
                course,
                semester,
                "Dr. Moradi",
                "01",
                true,
                30,
                "Mon 08:00",
                semester.getEndDate().minusWeeks(2)
        );

        student = new Student("Reza Karimi", "401122334", major, EnrollmentPlanType.REGULAR);
    }

    @Test
    @DisplayName("Should initialize enrollment record in IN_PROGRESS status and null grade")
    void shouldInitializeWithInProgressStatusAndNullGrade() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        assertThat(record.getStudent()).isEqualTo(student);
        assertThat(record.getCourseOffering()).isEqualTo(offering);
        assertThat(record.getStatus()).isEqualTo(CourseStatus.IN_PROGRESS);
        assertThat(record.getGrade()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when student is null in constructor")
    void shouldThrowExceptionWhenStudentIsNull() {
        assertThatThrownBy(() -> new EnrollmentRecord(null, offering))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Student cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when course offering is null in constructor")
    void shouldThrowExceptionWhenCourseOfferingIsNull() {
        assertThatThrownBy(() -> new EnrollmentRecord(student, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CourseOffering cannot be null");
    }

    @Test
    @DisplayName("Should successfully assign passing grade and change status to PASSED")
    void shouldAssignPassingGradeAndSetStatusToPassed() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        record.assignGrade(PASSING_GRADE);

        assertThat(record.getGrade()).isEqualTo(PASSING_GRADE);
        assertThat(record.getStatus()).isEqualTo(CourseStatus.PASSED);
    }

    @Test
    @DisplayName("Should assign failing grade and change status to FAILED")
    void shouldAssignFailingGradeAndSetStatusToFailed() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        record.assignGrade(FAILING_GRADE);

        assertThat(record.getGrade()).isEqualTo(FAILING_GRADE);
        assertThat(record.getStatus()).isEqualTo(CourseStatus.FAILED);
    }

    @Test
    @DisplayName("Should successfully assign minimum and maximum boundary grades")
    void shouldAssignBoundaryGrades() {
        EnrollmentRecord record1 = new EnrollmentRecord(student, offering);
        record1.assignGrade(MIN_GRADE);
        assertThat(record1.getGrade()).isEqualTo(MIN_GRADE);
        assertThat(record1.getStatus()).isEqualTo(CourseStatus.FAILED);

        EnrollmentRecord record2 = new EnrollmentRecord(student, offering);
        record2.assignGrade(MAX_GRADE);
        assertThat(record2.getGrade()).isEqualTo(MAX_GRADE);
        assertThat(record2.getStatus()).isEqualTo(CourseStatus.PASSED);
    }

    @Test
    @DisplayName("Should throw exception when grade is below zero")
    void shouldThrowExceptionWhenGradeIsBelowZero() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        assertThatThrownBy(() -> record.assignGrade(BELOW_MIN_GRADE))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when grade is above twenty")
    void shouldThrowExceptionWhenGradeIsAboveTwenty() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        assertThatThrownBy(() -> record.assignGrade(ABOVE_MAX_GRADE))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when grade is null")
    void shouldThrowExceptionWhenGradeIsNull() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        assertThatThrownBy(() -> record.assignGrade(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Grade cannot be null");
    }

    @Test
    @DisplayName("Should allow at most two decimal places for grade")
    void shouldAllowAtMostTwoDecimalPlaces() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        record.assignGrade(TWO_DECIMAL_GRADE);
        assertThat(record.getGrade()).isEqualTo(TWO_DECIMAL_GRADE);

        EnrollmentRecord recordWithThreeDecimals = new EnrollmentRecord(student, offering);
        assertThatThrownBy(() -> recordWithThreeDecimals.assignGrade(THREE_DECIMAL_GRADE))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("decimal places");
    }

    @Test
    @DisplayName("Should throw exception when assigning grade to already completed course")
    void shouldThrowExceptionWhenAssigningGradeToNonInProgressRecord() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        record.assignGrade(PASSING_GRADE);

        assertThatThrownBy(() -> record.assignGrade(MAX_GRADE))
                .isInstanceOf(InvalidEnrollmentStateException.class);
    }

    @Test
    @DisplayName("Should mark in-progress course as passed")
    void shouldMarkAsPassed() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        record.markAsPassed();

        assertThat(record.getStatus()).isEqualTo(CourseStatus.PASSED);
    }

    @Test
    @DisplayName("Should mark in-progress course as failed")
    void shouldMarkAsFailed() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        record.markAsFailed();

        assertThat(record.getStatus()).isEqualTo(CourseStatus.FAILED);
    }

    @Test
    @DisplayName("Should throw exception when marking already completed course as passed or failed")
    void shouldThrowExceptionWhenMarkingCompletedCourse() {
        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        record.markAsPassed();

        assertThatThrownBy(record::markAsPassed)
                .isInstanceOf(InvalidEnrollmentStateException.class);

        assertThatThrownBy(record::markAsFailed)
                .isInstanceOf(InvalidEnrollmentStateException.class);
    }
}
