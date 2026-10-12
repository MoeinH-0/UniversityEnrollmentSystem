package com.example.universityenrollmentsystem.service.semesters.reschedule;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RescheduleSemesterCommandValidatorTest {

    private static final Long VALID_SEMESTER_ID = 1L;
    private static final LocalDate VALID_START = LocalDate.of(2026, 9, 23);
    private static final LocalDate VALID_END = LocalDate.of(2027, 1, 20);

    private RescheduleSemesterCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RescheduleSemesterCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(VALID_SEMESTER_ID, VALID_START, VALID_END);

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
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(null, VALID_START, VALID_END);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newStartDate is null")
    void shouldThrowExceptionWhenStartDateIsNull() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(VALID_SEMESTER_ID, null, VALID_END);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newEndDate is null")
    void shouldThrowExceptionWhenEndDateIsNull() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(VALID_SEMESTER_ID, VALID_START, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newEndDate is before newStartDate")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        RescheduleSemesterCommand command = new RescheduleSemesterCommand(VALID_SEMESTER_ID, VALID_END, VALID_START);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
