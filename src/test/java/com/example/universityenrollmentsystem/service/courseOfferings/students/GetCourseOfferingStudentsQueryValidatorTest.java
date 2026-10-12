package com.example.universityenrollmentsystem.service.courseOfferings.students;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCourseOfferingStudentsQueryValidatorTest {

    private static final Long VALID_OFFERING_ID = 1L;

    private GetCourseOfferingStudentsQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetCourseOfferingStudentsQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetCourseOfferingStudentsQuery query = new GetCourseOfferingStudentsQuery(VALID_OFFERING_ID);

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
    @DisplayName("Should throw CommandValidationException when courseOfferingId is null")
    void shouldThrowExceptionWhenOfferingIdIsNull() {
        GetCourseOfferingStudentsQuery query = new GetCourseOfferingStudentsQuery(null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
