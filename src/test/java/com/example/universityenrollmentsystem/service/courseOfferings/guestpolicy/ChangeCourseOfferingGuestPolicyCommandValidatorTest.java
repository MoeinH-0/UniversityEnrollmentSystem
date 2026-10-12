package com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeCourseOfferingGuestPolicyCommandValidatorTest {

    private static final Long VALID_OFFERING_ID = 10L;

    private ChangeCourseOfferingGuestPolicyCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChangeCourseOfferingGuestPolicyCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        ChangeCourseOfferingGuestPolicyCommand command = new ChangeCourseOfferingGuestPolicyCommand(VALID_OFFERING_ID, true);

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
        ChangeCourseOfferingGuestPolicyCommand command = new ChangeCourseOfferingGuestPolicyCommand(null, true);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
