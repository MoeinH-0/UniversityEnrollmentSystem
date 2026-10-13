package com.example.universityenrollmentsystem.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseE2ETest {

    @LocalServerPort
    private int port;

    protected TestRestClient restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupBase() {
        this.restTemplate = new TestRestClient(port);

        jdbcTemplate.execute("TRUNCATE TABLE enrollment_records, course_offerings, course_prerequisites, major_courses, students, courses, semesters, majors CASCADE");
    }

    @AfterEach
    void tearDownBase() {
        jdbcTemplate.execute("TRUNCATE TABLE enrollment_records, course_offerings, course_prerequisites, major_courses, students, courses, semesters, majors CASCADE");
    }

    protected HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> HttpEntity<T> createJsonEntity(T body) {
        return new HttpEntity<>(body, createJsonHeaders());
    }
}
