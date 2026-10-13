package com.example.universityenrollmentsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String TITLE = "University Enrollment System API";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "RESTful API for University Course Enrollment Management System";
    private static final String CONTACT_NAME = "University IT Dept";
    private static final String CONTACT_EMAIL = "admin@university.edu";

    @Bean
    public OpenAPI universityEnrollmentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(VERSION)
                        .description(DESCRIPTION)
                        .contact(new Contact().name(CONTACT_NAME).email(CONTACT_EMAIL)));
    }
}
