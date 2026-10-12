package com.example.universityenrollmentsystem.service.majors.archive;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveMajorCommandValidatorTest {

    private static final Long VALID_MAJOR_ID = 1L;

    private ArchiveMajorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArchiveMajorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid majorId")
    void shouldValidateSuccessfully() {
        ArchiveMajorCommand command = new ArchiveMajorCommand(VALID_MAJOR_ID);

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
        ArchiveMajorCommand command = new ArchiveMajorCommand(null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
