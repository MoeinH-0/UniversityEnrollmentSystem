package com.example.universityenrollmentsystem.service.courseOfferings.professor;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeCourseOfferingProfessorCommandValidatorTest {

    private static final Long VALID_OFFERING_ID = 10L;
    private static final String VALID_PROFESSOR = "Dr. Karimi";

    private ChangeCourseOfferingProfessorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChangeCourseOfferingProfessorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        ChangeCourseOfferingProfessorCommand command = new ChangeCourseOfferingProfessorCommand(VALID_OFFERING_ID, VALID_PROFESSOR);

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
    @DisplayName("Should throw CommandValidationException when courseOfferingId is null")
    void shouldThrowExceptionWhenOfferingIdIsNull() {
        ChangeCourseOfferingProfessorCommand command = new ChangeCourseOfferingProfessorCommand(null, VALID_PROFESSOR);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newProfessorName is null or blank")
    void shouldThrowExceptionWhenProfessorNameIsBlank() {
        ChangeCourseOfferingProfessorCommand nullCmd = new ChangeCourseOfferingProfessorCommand(VALID_OFFERING_ID, null);

        assertThatThrownBy(() -> validator.validate(nullCmd))
                .isInstanceOf(CommandValidationException.class);

        ChangeCourseOfferingProfessorCommand blankCmd = new ChangeCourseOfferingProfessorCommand(VALID_OFFERING_ID, "   ");

        assertThatThrownBy(() -> validator.validate(blankCmd))
                .isInstanceOf(CommandValidationException.class);
    }
}
