package com.example.universityenrollmentsystem.service.courseOfferings.cancel;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelCourseOfferingCommandValidatorTest {

    private static final Long VALID_OFFERING_ID = 10L;

    private CancelCourseOfferingCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CancelCourseOfferingCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid courseOfferingId")
    void shouldValidateSuccessfully() {
        CancelCourseOfferingCommand command = new CancelCourseOfferingCommand(VALID_OFFERING_ID);

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
        CancelCourseOfferingCommand command = new CancelCourseOfferingCommand(null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
