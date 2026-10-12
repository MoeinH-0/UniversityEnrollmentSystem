package com.example.universityenrollmentsystem.service.semesters.archive;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveSemesterCommandValidatorTest {

    private static final Long VALID_SEMESTER_ID = 1L;

    private ArchiveSemesterCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArchiveSemesterCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid semesterId")
    void shouldValidateSuccessfully() {
        ArchiveSemesterCommand command = new ArchiveSemesterCommand(VALID_SEMESTER_ID);

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
    @DisplayName("Should throw CommandValidationException when semesterId is null")
    void shouldThrowExceptionWhenSemesterIdIsNull() {
        ArchiveSemesterCommand command = new ArchiveSemesterCommand(null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
