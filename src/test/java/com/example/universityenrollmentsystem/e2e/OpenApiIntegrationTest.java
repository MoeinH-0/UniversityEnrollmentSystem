package com.example.universityenrollmentsystem.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiIntegrationTest extends BaseE2ETest {

    private static final String API_DOCS_URL = "/api-docs";
    private static final String SWAGGER_UI_URL = "/swagger-ui.html";
    private static final String API_TITLE = "University Enrollment System API";
    private static final String STUDENTS_PATH = "/api/v1/students";
    private static final String COURSES_PATH = "/api/v1/courses";

    @Test
    @DisplayName("Should serve OpenAPI JSON definition successfully")
    void shouldServeOpenApiJson() {
        ResponseEntity<String> response = restTemplate.getForEntity(API_DOCS_URL, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(API_TITLE);
        assertThat(response.getBody()).contains(STUDENTS_PATH);
        assertThat(response.getBody()).contains(COURSES_PATH);
    }

    @Test
    @DisplayName("Should serve Swagger UI endpoint")
    void shouldServeSwaggerUi() {
        ResponseEntity<String> response = restTemplate.getForEntity(SWAGGER_UI_URL, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()).isTrue();
    }
}
