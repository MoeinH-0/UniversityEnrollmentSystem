package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetEligibleCoursesQueryValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;
    private static final Long VALID_SEMESTER_ID = 10L;

    private GetEligibleCoursesQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetEligibleCoursesQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(VALID_STUDENT_ID, VALID_SEMESTER_ID);

        assertThatCode(() -> validator.validate(query))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw CommandValidationException when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when studentId is null")
    void shouldThrowExceptionWhenStudentIdIsNull() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(null, VALID_SEMESTER_ID);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when semesterId is null")
    void shouldThrowExceptionWhenSemesterIdIsNull() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(VALID_STUDENT_ID, null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
