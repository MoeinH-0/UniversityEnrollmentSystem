package com.example.universityenrollmentsystem.service.enrollments.drop;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DropCourseOfferingCommandValidatorTest {

    private DropCourseOfferingCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DropCourseOfferingCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains all required fields")
    void shouldValidateSuccessfully() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(10L, 1L);

        assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when courseOfferingId is null")
    void shouldThrowExceptionWhenCourseOfferingIdIsNull() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(null, 1L);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw exception when studentId is null")
    void shouldThrowExceptionWhenStudentIdIsNull() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(10L, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
