package com.example.universityenrollmentsystem.service.students.status;

import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeStudentStatusCommandValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;
    private static final StudentStatus VALID_STATUS = StudentStatus.ACTIVE;

    private ChangeStudentStatusCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChangeStudentStatusCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        ChangeStudentStatusCommand command = new ChangeStudentStatusCommand(VALID_STUDENT_ID, VALID_STATUS);

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
        ChangeStudentStatusCommand command = new ChangeStudentStatusCommand(null, VALID_STATUS);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newStatus is null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        ChangeStudentStatusCommand command = new ChangeStudentStatusCommand(VALID_STUDENT_ID, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
