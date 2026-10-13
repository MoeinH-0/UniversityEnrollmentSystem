package com.example.universityenrollmentsystem.controller.advice;

import com.example.universityenrollmentsystem.domain.exception.DomainException;
import com.example.universityenrollmentsystem.domain.exception.rule.DuplicateEnrollmentException;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final String NOT_FOUND_URL = "/test/not-found";
    private static final String CONFLICT_URL = "/test/conflict";
    private static final String DUPLICATE_URL = "/test/duplicate";
    private static final String COMMAND_VALIDATION_URL = "/test/command-validation";
    private static final String DOMAIN_VALIDATION_URL = "/test/domain-validation";
    private static final String DOMAIN_RULE_URL = "/test/domain-rule";
    private static final String INTERNAL_ERROR_URL = "/test/internal-error";
    private static final String BLANK_ERROR_URL = "/test/blank-error";

    private static final String NOT_FOUND_MESSAGE = "Entity not found";
    private static final String CONFLICT_MESSAGE = "Conflict occurred";
    private static final String DUPLICATE_MESSAGE = "Duplicate enrollment";
    private static final String VALIDATION_MESSAGE = "Validation error";
    private static final String DOMAIN_MESSAGE = "Domain rule violation";
    private static final String UNEXPECTED_MESSAGE = "Something went wrong";
    private static final String DEFAULT_INTERNAL_MESSAGE = "An unexpected error occurred";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 404 Not Found when BaseNotFoundException is thrown")
    void shouldHandleNotFoundException() throws Exception {
        mockMvc.perform(get(NOT_FOUND_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Should return 409 Conflict when BaseConflictException is thrown")
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(get(CONFLICT_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(CONFLICT_MESSAGE));
    }

    @Test
    @DisplayName("Should return 409 Conflict when DuplicateEnrollmentException is thrown")
    void shouldHandleDuplicateEnrollmentException() throws Exception {
        mockMvc.perform(get(DUPLICATE_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(DUPLICATE_MESSAGE));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CommandValidationException is thrown")
    void shouldHandleCommandValidationException() throws Exception {
        mockMvc.perform(get(COMMAND_VALIDATION_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(VALIDATION_MESSAGE));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when DomainValidationException is thrown")
    void shouldHandleDomainValidationException() throws Exception {
        mockMvc.perform(get(DOMAIN_VALIDATION_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(VALIDATION_MESSAGE));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when DomainException is thrown")
    void shouldHandleDomainException() throws Exception {
        mockMvc.perform(get(DOMAIN_RULE_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(DOMAIN_MESSAGE));
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when unhandled exception is thrown")
    void shouldHandleGeneralException() throws Exception {
        mockMvc.perform(get(INTERNAL_ERROR_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(UNEXPECTED_MESSAGE));
    }

    @Test
    @DisplayName("Should return default message on 500 Internal Server Error when exception message is blank")
    void shouldHandleGeneralExceptionWithBlankMessage() throws Exception {
        mockMvc.perform(get(BLANK_ERROR_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(DEFAULT_INTERNAL_MESSAGE));
    }

    @RestController
    @RequestMapping("/test")
    static class ExceptionTestController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new CourseNotFoundException(1L) {
                @Override
                public String getMessage() {
                    return NOT_FOUND_MESSAGE;
                }
            };
        }

        @GetMapping("/conflict")
        public void throwConflict() {
            throw new CourseAlreadyExistsException("CS101") {
                @Override
                public String getMessage() {
                    return CONFLICT_MESSAGE;
                }
            };
        }

        @GetMapping("/duplicate")
        public void throwDuplicate() {
            throw new DuplicateEnrollmentException(DUPLICATE_MESSAGE);
        }

        @GetMapping("/command-validation")
        public void throwCommandValidation() {
            throw new CommandValidationException(VALIDATION_MESSAGE);
        }

        @GetMapping("/domain-validation")
        public void throwDomainValidation() {
            throw new DomainValidationException(VALIDATION_MESSAGE);
        }

        @GetMapping("/domain-rule")
        public void throwDomainRule() {
            throw new DomainException(DOMAIN_MESSAGE) {};
        }

        @GetMapping("/internal-error")
        public void throwInternalError() {
            throw new RuntimeException(UNEXPECTED_MESSAGE);
        }

        @GetMapping("/blank-error")
        public void throwBlankError() {
            throw new RuntimeException("");
        }
    }
}
