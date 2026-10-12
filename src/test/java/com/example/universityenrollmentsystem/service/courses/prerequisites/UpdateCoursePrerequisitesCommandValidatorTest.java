package com.example.universityenrollmentsystem.service.courses.prerequisites;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateCoursePrerequisitesCommandValidatorTest {

    private static final Long VALID_COURSE_ID = 1L;

    private UpdateCoursePrerequisitesCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UpdateCoursePrerequisitesCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid courseId")
    void shouldValidateSuccessfully() {
        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(VALID_COURSE_ID, List.of(2L));

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
        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(null, List.of(2L));

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
