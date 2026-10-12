package com.example.universityenrollmentsystem.service.courseOfferings.exam;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RescheduleCourseOfferingExamCommandValidatorTest {

    private static final Long VALID_OFFERING_ID = 10L;
    private static final LocalDate VALID_EXAM_DATE = LocalDate.of(2027, 1, 15);

    private RescheduleCourseOfferingExamCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RescheduleCourseOfferingExamCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        RescheduleCourseOfferingExamCommand command = new RescheduleCourseOfferingExamCommand(VALID_OFFERING_ID, VALID_EXAM_DATE);

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
        RescheduleCourseOfferingExamCommand command = new RescheduleCourseOfferingExamCommand(null, VALID_EXAM_DATE);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newExamDate is null")
    void shouldThrowExceptionWhenExamDateIsNull() {
        RescheduleCourseOfferingExamCommand command = new RescheduleCourseOfferingExamCommand(VALID_OFFERING_ID, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
