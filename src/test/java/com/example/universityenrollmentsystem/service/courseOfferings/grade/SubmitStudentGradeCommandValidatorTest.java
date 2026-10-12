package com.example.universityenrollmentsystem.service.courseOfferings.grade;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitStudentGradeCommandValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;
    private static final Long VALID_OFFERING_ID = 10L;
    private static final Double VALID_GRADE = 18.5;

    private SubmitStudentGradeCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SubmitStudentGradeCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains all valid fields")
    void shouldValidateSuccessfully() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(VALID_STUDENT_ID, VALID_OFFERING_ID, VALID_GRADE);

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
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(null, VALID_OFFERING_ID, VALID_GRADE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when courseOfferingId is null")
    void shouldThrowExceptionWhenOfferingIdIsNull() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(VALID_STUDENT_ID, null, VALID_GRADE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when grade is null")
    void shouldThrowExceptionWhenGradeIsNull() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(VALID_STUDENT_ID, VALID_OFFERING_ID, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
