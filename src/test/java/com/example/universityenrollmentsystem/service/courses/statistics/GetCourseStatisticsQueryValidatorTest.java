package com.example.universityenrollmentsystem.service.courses.statistics;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCourseStatisticsQueryValidatorTest {

    private static final Long VALID_COURSE_ID = 1L;

    private GetCourseStatisticsQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetCourseStatisticsQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetCourseStatisticsQuery query = new GetCourseStatisticsQuery(VALID_COURSE_ID);

        assertThatCode(() -> validator.validate(query))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw CommandValidationException when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when courseId is null")
    void shouldThrowExceptionWhenCourseIdIsNull() {
        GetCourseStatisticsQuery query = new GetCourseStatisticsQuery(null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
