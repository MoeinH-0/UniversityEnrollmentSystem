package com.example.universityenrollmentsystem.service.majors.create;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateMajorCommandValidatorTest {

    private static final String VALID_NAME = "Computer Engineering";

    private CreateMajorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CreateMajorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when name is valid")
    void shouldValidateSuccessfully() {
        CreateMajorCommand command = new CreateMajorCommand(VALID_NAME);

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
    @DisplayName("Should throw CommandValidationException when name is null or blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        CreateMajorCommand nullName = new CreateMajorCommand(null);

        assertThatThrownBy(() -> validator.validate(nullName))
                .isInstanceOf(CommandValidationException.class);

        CreateMajorCommand blankName = new CreateMajorCommand("   ");

        assertThatThrownBy(() -> validator.validate(blankName))
                .isInstanceOf(CommandValidationException.class);
    }
}
