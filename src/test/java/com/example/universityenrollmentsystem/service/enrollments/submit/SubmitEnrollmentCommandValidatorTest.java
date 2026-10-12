package com.example.universityenrollmentsystem.service.enrollments.submit;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitEnrollmentCommandValidatorTest {

    private SubmitEnrollmentCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SubmitEnrollmentCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains all required fields")
    void shouldValidateSuccessfully() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(1L, 10L);

        assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("Command cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when studentId is null")
    void shouldThrowExceptionWhenStudentIdIsNull() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(null, 10L);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("StudentId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when courseOfferingId is null")
    void shouldThrowExceptionWhenCourseOfferingIdIsNull() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(1L, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("CourseOfferingId cannot be null");
    }
}
