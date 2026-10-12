package com.example.universityenrollmentsystem.service.majors.addcourse;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddCourseToMajorCommandValidatorTest {

    private static final Long VALID_MAJOR_ID = 1L;
    private static final Long VALID_COURSE_ID = 10L;

    private AddCourseToMajorCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AddCourseToMajorCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid fields")
    void shouldValidateSuccessfully() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(VALID_MAJOR_ID, VALID_COURSE_ID);

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
    @DisplayName("Should throw CommandValidationException when majorId is null")
    void shouldThrowExceptionWhenMajorIdIsNull() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(null, VALID_COURSE_ID);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when courseId is null")
    void shouldThrowExceptionWhenCourseIdIsNull() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(VALID_MAJOR_ID, null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
