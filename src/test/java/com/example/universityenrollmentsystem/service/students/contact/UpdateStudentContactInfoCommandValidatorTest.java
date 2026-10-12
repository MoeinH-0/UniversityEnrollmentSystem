package com.example.universityenrollmentsystem.service.students.contact;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateStudentContactInfoCommandValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;
    private static final String VALID_EMAIL = "student@university.ac.ir";
    private static final String VALID_PHONE = "09123456789";

    private UpdateStudentContactInfoCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UpdateStudentContactInfoCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        UpdateStudentContactInfoCommand command = new UpdateStudentContactInfoCommand(VALID_STUDENT_ID, VALID_EMAIL, VALID_PHONE);

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
    @DisplayName("Should throw CommandValidationException when studentId is null")
    void shouldThrowExceptionWhenStudentIdIsNull() {
        UpdateStudentContactInfoCommand command = new UpdateStudentContactInfoCommand(null, VALID_EMAIL, VALID_PHONE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
