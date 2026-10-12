package com.example.universityenrollmentsystem.domain.entity.course;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseOfferingTest {

    private static final int VALID_CAPACITY = 30;
    private static final int ZERO_CAPACITY = 0;
    private static final int NEGATIVE_CAPACITY = -5;

    private Course course;
    private Semester semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate validExamDate;

    @BeforeEach
    void setUp() {
        course = new Course("Computer Architecture", "CS301", 3);
        startDate = LocalDate.of(2026, 9, 23);
        endDate = LocalDate.of(2027, 1, 20);
        semester = new Semester("Fall 2026", startDate, endDate);
        validExamDate = LocalDate.of(2027, 1, 10);
    }

    @Test
    @DisplayName("Should initialize course offering successfully with valid arguments")
    void shouldInitializeCourseOfferingSuccessfully() {
        CourseOffering offering = new CourseOffering(
                course,
                semester,
                "Dr. Hosseini",
                "01",
                true,
                VALID_CAPACITY,
                "Mon 10:00",
                validExamDate
        );

        assertThat(offering.getCourse()).isEqualTo(course);
        assertThat(offering.getSemester()).isEqualTo(semester);
        assertThat(offering.getProfessorName()).isEqualTo("Dr. Hosseini");
        assertThat(offering.getSection()).isEqualTo("01");
        assertThat(offering.isGuestAllowed()).isTrue();
        assertThat(offering.getCapacity()).isEqualTo(VALID_CAPACITY);
        assertThat(offering.getClassTime()).isEqualTo("Mon 10:00");
        assertThat(offering.getExamDate()).isEqualTo(validExamDate);
        assertThat(offering.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should allow null exam date")
    void shouldAllowNullExamDate() {
        CourseOffering offering = new CourseOffering(
                course,
                semester,
                "Dr. Hosseini",
                "01",
                true,
                VALID_CAPACITY,
                "Mon 10:00",
                null
        );

        assertThat(offering.getExamDate()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when course or semester is null")
    void shouldThrowExceptionWhenCourseOrSemesterNull() {
        assertThatThrownBy(() -> new CourseOffering(null, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new CourseOffering(course, null, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when professor name or section is blank")
    void shouldThrowExceptionWhenProfessorOrSectionBlank() {
        assertThatThrownBy(() -> new CourseOffering(course, semester, "  ", "01", true, VALID_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new CourseOffering(course, semester, "Dr. H", " ", true, VALID_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when capacity is zero or negative")
    void shouldThrowExceptionWhenCapacityZeroOrNegative() {
        assertThatThrownBy(() -> new CourseOffering(course, semester, "Dr. H", "01", true, ZERO_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new CourseOffering(course, semester, "Dr. H", "01", true, NEGATIVE_CAPACITY, "Time", validExamDate))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when exam date is outside semester bounds")
    void shouldThrowExceptionWhenExamDateOutsideSemester() {
        LocalDate beforeStart = startDate.minusDays(1);
        LocalDate afterEnd = endDate.plusDays(1);

        assertThatThrownBy(() -> new CourseOffering(course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", beforeStart))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> new CourseOffering(course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", afterEnd))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should update properties and validate new values")
    void shouldUpdateProperties() {
        CourseOffering offering = new CourseOffering(
                course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate
        );

        offering.changeProfessor("Dr. Sadeghi");
        offering.updateSection("02");
        offering.updateCapacity(40);
        offering.updateClassTime("Wed 14:00");
        offering.updateGuestPolicy(false);

        assertThat(offering.getProfessorName()).isEqualTo("Dr. Sadeghi");
        assertThat(offering.getSection()).isEqualTo("02");
        assertThat(offering.getCapacity()).isEqualTo(40);
        assertThat(offering.getClassTime()).isEqualTo("Wed 14:00");
        assertThat(offering.isGuestAllowed()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when updating capacity to non-positive number")
    void shouldThrowExceptionWhenUpdatingCapacityToNonPositive() {
        CourseOffering offering = new CourseOffering(
                course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate
        );

        assertThatThrownBy(() -> offering.updateCapacity(ZERO_CAPACITY))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Should mark course offering as deleted")
    void shouldMarkAsDeleted() {
        CourseOffering offering = new CourseOffering(
                course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate
        );

        offering.markAsDeleted();

        assertThat(offering.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should allow exam dates exactly on semester start and end date boundaries")
    void shouldAllowExamDatesOnSemesterBoundaries() {
        CourseOffering offeringStart = new CourseOffering(
                course, semester, "Dr. H", "01", true, VALID_CAPACITY, null, startDate
        );
        assertThat(offeringStart.getExamDate()).isEqualTo(startDate);
        assertThat(offeringStart.getClassTime()).isNull();

        CourseOffering offeringEnd = new CourseOffering(
                course, semester, "Dr. H", "01", true, 1, "Time", endDate
        );
        assertThat(offeringEnd.getExamDate()).isEqualTo(endDate);
        assertThat(offeringEnd.getCapacity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update exam date to null or boundary dates successfully")
    void shouldUpdateExamDateBoundariesAndNull() {
        CourseOffering offering = new CourseOffering(
                course, semester, "Dr. H", "01", true, VALID_CAPACITY, "Time", validExamDate
        );

        offering.updateExamDate(startDate);
        assertThat(offering.getExamDate()).isEqualTo(startDate);

        offering.updateExamDate(endDate);
        assertThat(offering.getExamDate()).isEqualTo(endDate);

        offering.updateExamDate(null);
        assertThat(offering.getExamDate()).isNull();

        offering.updateClassTime(null);
        assertThat(offering.getClassTime()).isNull();
    }
}
