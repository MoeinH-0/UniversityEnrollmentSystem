package com.example.universityenrollmentsystem.service.students.registration;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterStudentCommandValidatorTest {

    private RegisterStudentCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RegisterStudentCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains all required fields")
    void shouldValidateSuccessfully() {
        RegisterStudentCommand command = new RegisterStudentCommand("Ali Reza", "401001", 1L, EnrollmentPlanType.REGULAR);

        assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when fullName is null or blank")
    void shouldThrowExceptionWhenFullNameNullOrBlank() {
        assertThatThrownBy(() -> validator.validate(new RegisterStudentCommand(null, "401001", 1L, EnrollmentPlanType.REGULAR)))
                .isInstanceOf(CommandValidationException.class);

        assertThatThrownBy(() -> validator.validate(new RegisterStudentCommand("  ", "401001", 1L, EnrollmentPlanType.REGULAR)))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when studentNumber is null or blank")
    void shouldThrowExceptionWhenStudentNumberNullOrBlank() {
        assertThatThrownBy(() -> validator.validate(new RegisterStudentCommand("Ali", null, 1L, EnrollmentPlanType.REGULAR)))
                .isInstanceOf(CommandValidationException.class);

        assertThatThrownBy(() -> validator.validate(new RegisterStudentCommand("Ali", "  ", 1L, EnrollmentPlanType.REGULAR)))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when majorId is null")
    void shouldThrowExceptionWhenMajorIdIsNull() {
        RegisterStudentCommand command = new RegisterStudentCommand("Ali", "401001", null, EnrollmentPlanType.REGULAR);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when planType is null")
    void shouldThrowExceptionWhenPlanTypeIsNull() {
        RegisterStudentCommand command = new RegisterStudentCommand("Ali", "401001", 1L, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
