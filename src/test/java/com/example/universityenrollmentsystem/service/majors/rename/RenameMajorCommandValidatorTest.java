package com.example.universityenrollmentsystem.service.majors.rename;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenameMajorCommandValidatorTest {

    private static final Long VALID_MAJOR_ID = 1L;
    private static final String VALID_NAME = "Software Engineering";

    private RenameMajorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RenameMajorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        RenameMajorCommand command = new RenameMajorCommand(VALID_MAJOR_ID, VALID_NAME);

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
    @DisplayName("Should throw CommandValidationException when majorId is null")
    void shouldThrowExceptionWhenMajorIdIsNull() {
        RenameMajorCommand command = new RenameMajorCommand(null, VALID_NAME);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newName is null or blank")
    void shouldThrowExceptionWhenNewNameIsBlank() {
        RenameMajorCommand nullName = new RenameMajorCommand(VALID_MAJOR_ID, null);

        assertThatThrownBy(() -> validator.validate(nullName))
                .isInstanceOf(CommandValidationException.class);

        RenameMajorCommand blankName = new RenameMajorCommand(VALID_MAJOR_ID, "   ");

        assertThatThrownBy(() -> validator.validate(blankName))
                .isInstanceOf(CommandValidationException.class);
    }
}
