package com.example.universityenrollmentsystem.service.semesters.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetSemesterDetailsQueryValidatorTest {

    private static final Long VALID_SEMESTER_ID = 1L;

    private GetSemesterDetailsQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetSemesterDetailsQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetSemesterDetailsQuery query = new GetSemesterDetailsQuery(VALID_SEMESTER_ID);

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
    @DisplayName("Should throw CommandValidationException when semesterId is null")
    void shouldThrowExceptionWhenSemesterIdIsNull() {
        GetSemesterDetailsQuery query = new GetSemesterDetailsQuery(null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
