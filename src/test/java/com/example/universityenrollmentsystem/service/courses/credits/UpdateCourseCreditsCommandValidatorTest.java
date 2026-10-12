package com.example.universityenrollmentsystem.service.courses.credits;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateCourseCreditsCommandValidatorTest {

    private static final Long VALID_COURSE_ID = 1L;
    private static final int VALID_CREDITS = 4;
    private static final int BELOW_MIN_CREDITS = Course.MIN_CREDITS - 1;
    private static final int ABOVE_MAX_CREDITS = Course.MAX_CREDITS + 1;

    private UpdateCourseCreditsCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UpdateCourseCreditsCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        UpdateCourseCreditsCommand command = new UpdateCourseCreditsCommand(VALID_COURSE_ID, VALID_CREDITS);

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
    @DisplayName("Should throw CommandValidationException when courseId is null")
    void shouldThrowExceptionWhenCourseIdIsNull() {
        UpdateCourseCreditsCommand command = new UpdateCourseCreditsCommand(null, VALID_CREDITS);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when credits are outside allowable range")
    void shouldThrowExceptionWhenCreditsOutsideRange() {
        UpdateCourseCreditsCommand belowMin = new UpdateCourseCreditsCommand(VALID_COURSE_ID, BELOW_MIN_CREDITS);

        assertThatThrownBy(() -> validator.validate(belowMin))
                .isInstanceOf(CommandValidationException.class);

        UpdateCourseCreditsCommand aboveMax = new UpdateCourseCreditsCommand(VALID_COURSE_ID, ABOVE_MAX_CREDITS);

        assertThatThrownBy(() -> validator.validate(aboveMax))
                .isInstanceOf(CommandValidationException.class);
    }
}
