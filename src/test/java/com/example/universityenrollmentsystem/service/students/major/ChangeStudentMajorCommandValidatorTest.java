package com.example.universityenrollmentsystem.service.students.major;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeStudentMajorCommandValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;
    private static final Long VALID_MAJOR_ID = 2L;

    private ChangeStudentMajorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChangeStudentMajorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(VALID_STUDENT_ID, VALID_MAJOR_ID);

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
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(null, VALID_MAJOR_ID);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newMajorId is null")
    void shouldThrowExceptionWhenNewMajorIdIsNull() {
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(VALID_STUDENT_ID, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
