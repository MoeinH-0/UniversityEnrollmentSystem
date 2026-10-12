package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeCourseOfferingCapacityCommandValidatorTest {

    private static final Long VALID_OFFERING_ID = 10L;
    private static final int VALID_CAPACITY = 35;
    private static final int ZERO_CAPACITY = 0;
    private static final int NEGATIVE_CAPACITY = -1;

    private ChangeCourseOfferingCapacityCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChangeCourseOfferingCapacityCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(VALID_OFFERING_ID, VALID_CAPACITY);

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
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(null, VALID_CAPACITY);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when newCapacity is zero or negative")
    void shouldThrowExceptionWhenCapacityIsNotPositive() {
        ChangeCourseOfferingCapacityCommand zeroCmd = new ChangeCourseOfferingCapacityCommand(VALID_OFFERING_ID, ZERO_CAPACITY);

        assertThatThrownBy(() -> validator.validate(zeroCmd))
                .isInstanceOf(CommandValidationException.class);

        ChangeCourseOfferingCapacityCommand negCmd = new ChangeCourseOfferingCapacityCommand(VALID_OFFERING_ID, NEGATIVE_CAPACITY);

        assertThatThrownBy(() -> validator.validate(negCmd))
                .isInstanceOf(CommandValidationException.class);
    }
}
