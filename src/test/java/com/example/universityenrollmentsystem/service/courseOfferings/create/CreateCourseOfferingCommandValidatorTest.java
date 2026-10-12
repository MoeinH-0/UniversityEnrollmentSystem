package com.example.universityenrollmentsystem.service.courseOfferings.create;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCourseOfferingCommandValidatorTest {

    private static final Long VALID_COURSE_ID = 1L;
    private static final Long VALID_SEMESTER_ID = 2L;
    private static final String VALID_PROFESSOR = "Dr. Hosseini";
    private static final String VALID_SECTION = "01";
    private static final boolean VALID_GUEST_ALLOWED = true;
    private static final int VALID_CAPACITY = 30;
    private static final int ZERO_CAPACITY = 0;
    private static final int NEGATIVE_CAPACITY = -10;
    private static final String VALID_CLASS_TIME = "Mon 10:00";
    private static final LocalDate VALID_EXAM_DATE = LocalDate.of(2027, 1, 15);

    private CreateCourseOfferingCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CreateCourseOfferingCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command has all valid fields")
    void shouldValidateSuccessfully() {
        CreateCourseOfferingCommand command = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, VALID_PROFESSOR, VALID_SECTION,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw CommandValidationException when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when courseId is null")
    void shouldThrowExceptionWhenCourseIdIsNull() {
        CreateCourseOfferingCommand command = new CreateCourseOfferingCommand(
                null, VALID_SEMESTER_ID, VALID_PROFESSOR, VALID_SECTION,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when semesterId is null")
    void shouldThrowExceptionWhenSemesterIdIsNull() {
        CreateCourseOfferingCommand command = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, null, VALID_PROFESSOR, VALID_SECTION,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when professorName is null or blank")
    void shouldThrowExceptionWhenProfessorNameIsBlank() {
        CreateCourseOfferingCommand nullProf = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, null, VALID_SECTION,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(nullProf))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseOfferingCommand blankProf = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, "   ", VALID_SECTION,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(blankProf))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when section is null or blank")
    void shouldThrowExceptionWhenSectionIsBlank() {
        CreateCourseOfferingCommand nullSection = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, VALID_PROFESSOR, null,
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(nullSection))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseOfferingCommand blankSection = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, VALID_PROFESSOR, "   ",
                VALID_GUEST_ALLOWED, VALID_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(blankSection))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when capacity is zero or negative")
    void shouldThrowExceptionWhenCapacityIsNotPositive() {
        CreateCourseOfferingCommand zeroCap = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, VALID_PROFESSOR, VALID_SECTION,
                VALID_GUEST_ALLOWED, ZERO_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(zeroCap))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseOfferingCommand negCap = new CreateCourseOfferingCommand(
                VALID_COURSE_ID, VALID_SEMESTER_ID, VALID_PROFESSOR, VALID_SECTION,
                VALID_GUEST_ALLOWED, NEGATIVE_CAPACITY, VALID_CLASS_TIME, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(negCap))
                .isInstanceOf(CommandValidationException.class);
    }
}
