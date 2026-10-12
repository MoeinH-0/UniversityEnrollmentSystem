package com.example.universityenrollmentsystem.service.majors.catalog;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMajorCatalogQueryValidatorTest {

    private static final Long VALID_MAJOR_ID = 1L;

    private GetMajorCatalogQueryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GetMajorCatalogQueryValidator();
    }

    @Test
    @DisplayName("Should validate successfully when query is valid")
    void shouldValidateSuccessfully() {
        GetMajorCatalogQuery query = new GetMajorCatalogQuery(VALID_MAJOR_ID);

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
    @DisplayName("Should throw CommandValidationException when majorId is null")
    void shouldThrowExceptionWhenMajorIdIsNull() {
        GetMajorCatalogQuery query = new GetMajorCatalogQuery(null);

        assertThatThrownBy(() -> validator.validate(query))
                .isInstanceOf(CommandValidationException.class);
    }
}
