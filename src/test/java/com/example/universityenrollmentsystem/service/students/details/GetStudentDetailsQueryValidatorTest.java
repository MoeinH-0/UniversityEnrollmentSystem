package com.example.universityenrollmentsystem.service.students.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetStudentDetailsQueryValidatorTest {

    private static final Long VALID_STUDENT_ID = 1L;

    private GetStudentDetailsQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetStudentDetailsQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetStudentDetailsQuery query = new GetStudentDetailsQuery(VALID_STUDENT_ID);

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
    @DisplayName("Should throw CommandValidationException when studentId is null")
    void shouldThrowExceptionWhenStudentIdIsNull() {
        GetStudentDetailsQuery query = new GetStudentDetailsQuery(null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
