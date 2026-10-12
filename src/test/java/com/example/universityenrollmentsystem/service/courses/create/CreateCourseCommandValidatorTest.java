package com.example.universityenrollmentsystem.service.courses.create;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCourseCommandValidatorTest {

    private static final String VALID_NAME = "Advanced Programming";
    private static final String VALID_CODE = "CS202";
    private static final int VALID_CREDITS = 3;
    private static final int BELOW_MIN_CREDITS = Course.MIN_CREDITS - 1;
    private static final int ABOVE_MAX_CREDITS = Course.MAX_CREDITS + 1;
    private static final List<Long> VALID_MAJOR_IDS = List.of(1L);

    private CreateCourseCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CreateCourseCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command has valid fields")
    void shouldValidateSuccessfully() {
        CreateCourseCommand command = new CreateCourseCommand(
                VALID_NAME, VALID_CODE, VALID_CREDITS, VALID_MAJOR_IDS, null);

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
    @DisplayName("Should throw CommandValidationException when name is null or blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        CreateCourseCommand nullName = new CreateCourseCommand(
                null, VALID_CODE, VALID_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(nullName))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseCommand blankName = new CreateCourseCommand(
                "   ", VALID_CODE, VALID_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(blankName))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when courseCode is null or blank")
    void shouldThrowExceptionWhenCourseCodeIsBlank() {
        CreateCourseCommand nullCode = new CreateCourseCommand(
                VALID_NAME, null, VALID_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(nullCode))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseCommand blankCode = new CreateCourseCommand(
                VALID_NAME, "  ", VALID_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(blankCode))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when credits are outside allowable range")
    void shouldThrowExceptionWhenCreditsOutsideRange() {
        CreateCourseCommand belowMin = new CreateCourseCommand(
                VALID_NAME, VALID_CODE, BELOW_MIN_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(belowMin))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseCommand aboveMax = new CreateCourseCommand(
                VALID_NAME, VALID_CODE, ABOVE_MAX_CREDITS, VALID_MAJOR_IDS, null);

        assertThatThrownBy(() -> validator.validate(aboveMax))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when majorIds is null or empty")
    void shouldThrowExceptionWhenMajorIdsIsNullOrEmpty() {
        CreateCourseCommand nullMajors = new CreateCourseCommand(
                VALID_NAME, VALID_CODE, VALID_CREDITS, null, null);

        assertThatThrownBy(() -> validator.validate(nullMajors))
                .isInstanceOf(CommandValidationException.class);

        CreateCourseCommand emptyMajors = new CreateCourseCommand(
                VALID_NAME, VALID_CODE, VALID_CREDITS, List.of(), null);

        assertThatThrownBy(() -> validator.validate(emptyMajors))
                .isInstanceOf(CommandValidationException.class);
    }
}
