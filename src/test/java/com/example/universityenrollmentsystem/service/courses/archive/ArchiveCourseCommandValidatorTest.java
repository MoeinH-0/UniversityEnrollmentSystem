package com.example.universityenrollmentsystem.service.courses.archive;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveCourseCommandValidatorTest {

    private static final Long VALID_COURSE_ID = 1L;

    private ArchiveCourseCommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArchiveCourseCommandValidator();
    }

    @Test
    @DisplayName("Should validate successfully when command contains valid courseId")
    void shouldValidateSuccessfully() {
        ArchiveCourseCommand command = new ArchiveCourseCommand(VALID_COURSE_ID);

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
        ArchiveCourseCommand command = new ArchiveCourseCommand(null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(CommandValidationException.class);
    }
}
