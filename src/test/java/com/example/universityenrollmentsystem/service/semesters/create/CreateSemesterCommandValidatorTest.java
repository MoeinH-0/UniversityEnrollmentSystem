package com.example.universityenrollmentsystem.service.semesters.create;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateSemesterCommandValidatorTest {

    private static final String VALID_TITLE = "Fall 2026";
    private static final LocalDate VALID_START = LocalDate.of(2026, 9, 23);
    private static final LocalDate VALID_END = LocalDate.of(2027, 1, 20);

    private CreateSemesterCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CreateSemesterCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        CreateSemesterCommand command = new CreateSemesterCommand(VALID_TITLE, VALID_START, VALID_END);

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
    @DisplayName("Should throw CommandValidationException when title is null or blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        CreateSemesterCommand nullTitle = new CreateSemesterCommand(null, VALID_START, VALID_END);

        assertThatThrownBy(() -> validator.validate(nullTitle))
                .isInstanceOf(CommandValidationException.class);

        CreateSemesterCommand blankTitle = new CreateSemesterCommand("   ", VALID_START, VALID_END);

        assertThatThrownBy(() -> validator.validate(blankTitle))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when startDate is null")
    void shouldThrowExceptionWhenStartDateIsNull() {
        CreateSemesterCommand command = new CreateSemesterCommand(VALID_TITLE, null, VALID_END);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when endDate is null")
    void shouldThrowExceptionWhenEndDateIsNull() {
        CreateSemesterCommand command = new CreateSemesterCommand(VALID_TITLE, VALID_START, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when endDate is before startDate")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        CreateSemesterCommand command = new CreateSemesterCommand(VALID_TITLE, VALID_END, VALID_START);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
