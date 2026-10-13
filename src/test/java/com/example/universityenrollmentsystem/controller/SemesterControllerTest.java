package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.service.exceptions.conflict.SemesterAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import com.example.universityenrollmentsystem.service.semesters.archive.ArchiveSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.archive.ArchiveSemesterCommandHandler;
import com.example.universityenrollmentsystem.service.semesters.create.CreateSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.create.CreateSemesterCommandHandler;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsQuery;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsQuery;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsQueryHandler;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsResponse;
import com.example.universityenrollmentsystem.service.semesters.reschedule.RescheduleSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.reschedule.RescheduleSemesterCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SemesterControllerTest {

    private static final Long SEMESTER_ID = 1L;
    private static final String TITLE = "Fall 2026";
    private static final String VALIDATION_ERROR = "Start date must be before end date";
    private static final Long COURSE_ID = 10L;
    private static final Long OFFERING_ID = 100L;
    private static final String COURSE_NAME = "AI";
    private static final String COURSE_CODE = "AI401";
    private static final int CREDITS = 3;
    private static final String PROFESSOR = "Dr. Hassani";
    private static final String SECTION = "01";
    private static final int CAPACITY = 25;
    private static final String CLASS_TIME = "Tue 10:00";

    private static final String BASE_URL = "/api/v1/semesters";
    private static final String SEMESTER_URL = "/api/v1/semesters/{id}";
    private static final String SCHEDULE_URL = "/api/v1/semesters/{id}/schedule";
    private static final String OFFERINGS_URL = "/api/v1/semesters/{id}/offerings";
    private static final String NON_NUMERIC_URL = "/api/v1/semesters/invalid-id";
    private static final String MALFORMED_JSON = "{\"title\": \"Fall 2026\", unclosed";

    @Mock
    private CreateSemesterCommandHandler createSemesterCommandHandler;

    @Mock
    private ArchiveSemesterCommandHandler archiveSemesterCommandHandler;

    @Mock
    private RescheduleSemesterCommandHandler rescheduleSemesterCommandHandler;

    @Mock
    private GetSemesterDetailsQueryHandler getSemesterDetailsQueryHandler;

    @Mock
    private GetSemesterCourseOfferingsQueryHandler getSemesterCourseOfferingsQueryHandler;

    @InjectMocks
    private SemesterController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with Location header when creating semester")
    void shouldCreateSemesterSuccessfully() throws Exception {
        when(createSemesterCommandHandler.handle(any(CreateSemesterCommand.class))).thenReturn(SEMESTER_ID);

        String jsonPayload = """
                {
                    "title": "Fall 2026",
                    "startDate": "2026-09-23",
                    "endDate": "2027-01-20"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/semesters/" + SEMESTER_ID))
                .andExpect(jsonPath("$").value(SEMESTER_ID));

        verify(createSemesterCommandHandler).handle(any(CreateSemesterCommand.class));
    }

    @Test
    @DisplayName("Should return 409 Conflict when semester title already exists")
    void shouldReturnConflictWhenSemesterTitleExists() throws Exception {
        when(createSemesterCommandHandler.handle(any(CreateSemesterCommand.class)))
                .thenThrow(new SemesterAlreadyExistsException(TITLE));

        String jsonPayload = """
                {
                    "title": "Fall 2026",
                    "startDate": "2026-09-23",
                    "endDate": "2027-01-20"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when create semester validation fails")
    void shouldReturnBadRequestWhenCreateSemesterValidationFails() throws Exception {
        when(createSemesterCommandHandler.handle(any(CreateSemesterCommand.class)))
                .thenThrow(new CommandValidationException(VALIDATION_ERROR));

        String jsonPayload = """
                {
                    "title": "Fall 2026",
                    "startDate": "2027-01-20",
                    "endDate": "2026-09-23"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when archiving semester")
    void shouldArchiveSemesterSuccessfully() throws Exception {
        mockMvc.perform(delete(SEMESTER_URL, SEMESTER_ID))
                .andExpect(status().isNoContent());

        verify(archiveSemesterCommandHandler).handle(new ArchiveSemesterCommand(SEMESTER_ID));
    }

    @Test
    @DisplayName("Should return 404 Not Found when archiving non-existent semester")
    void shouldReturnNotFoundWhenArchivingNonExistentSemester() throws Exception {
        doThrow(new SemesterNotFoundException(SEMESTER_ID))
                .when(archiveSemesterCommandHandler).handle(new ArchiveSemesterCommand(SEMESTER_ID));

        mockMvc.perform(delete(SEMESTER_URL, SEMESTER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when rescheduling semester")
    void shouldRescheduleSemesterSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "startDate": "2026-10-01",
                    "endDate": "2027-02-01"
                }
                """;

        mockMvc.perform(put(SCHEDULE_URL, SEMESTER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(rescheduleSemesterCommandHandler).handle(any(RescheduleSemesterCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when rescheduling non-existent semester")
    void shouldReturnNotFoundWhenReschedulingNonExistentSemester() throws Exception {
        doThrow(new SemesterNotFoundException(SEMESTER_ID))
                .when(rescheduleSemesterCommandHandler).handle(any(RescheduleSemesterCommand.class));

        String jsonPayload = """
                {
                    "startDate": "2026-10-01",
                    "endDate": "2027-02-01"
                }
                """;

        mockMvc.perform(put(SCHEDULE_URL, SEMESTER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when reschedule semester validation fails")
    void shouldReturnBadRequestWhenRescheduleValidationFails() throws Exception {
        doThrow(new CommandValidationException(VALIDATION_ERROR))
                .when(rescheduleSemesterCommandHandler).handle(any(RescheduleSemesterCommand.class));

        String jsonPayload = """
                {
                    "startDate": "2027-02-01",
                    "endDate": "2026-10-01"
                }
                """;

        mockMvc.perform(put(SCHEDULE_URL, SEMESTER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 200 OK with semester details")
    void shouldGetSemesterDetailsSuccessfully() throws Exception {
        GetSemesterDetailsResponse response = new GetSemesterDetailsResponse(
                SEMESTER_ID, TITLE, LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20), false
        );

        when(getSemesterDetailsQueryHandler.handle(new GetSemesterDetailsQuery(SEMESTER_ID)))
                .thenReturn(response);

        mockMvc.perform(get(SEMESTER_URL, SEMESTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterId").value(SEMESTER_ID))
                .andExpect(jsonPath("$.title").value(TITLE));
    }

    @Test
    @DisplayName("Should return 404 Not Found when semester details requested for non-existent semester")
    void shouldReturnNotFoundWhenSemesterDetailsRequestedForNonExistentSemester() throws Exception {
        when(getSemesterDetailsQueryHandler.handle(new GetSemesterDetailsQuery(SEMESTER_ID)))
                .thenThrow(new SemesterNotFoundException(SEMESTER_ID));

        mockMvc.perform(get(SEMESTER_URL, SEMESTER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with semester course offerings")
    void shouldGetSemesterCourseOfferingsSuccessfully() throws Exception {
        GetSemesterCourseOfferingsResponse.SemesterCourseOfferingDto offeringDto =
                new GetSemesterCourseOfferingsResponse.SemesterCourseOfferingDto(
                        COURSE_ID, OFFERING_ID, COURSE_NAME, COURSE_CODE, CREDITS, PROFESSOR, SECTION,
                        CAPACITY, CLASS_TIME, LocalDate.of(2027, 1, 10), true
                );

        GetSemesterCourseOfferingsResponse response = new GetSemesterCourseOfferingsResponse(
                SEMESTER_ID, TITLE, List.of(offeringDto)
        );

        when(getSemesterCourseOfferingsQueryHandler.handle(new GetSemesterCourseOfferingsQuery(SEMESTER_ID)))
                .thenReturn(response);

        mockMvc.perform(get(OFFERINGS_URL, SEMESTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterId").value(SEMESTER_ID))
                .andExpect(jsonPath("$.offerings[0].courseName").value(COURSE_NAME));
    }

    @Test
    @DisplayName("Should return 404 Not Found when semester offerings requested for non-existent semester")
    void shouldReturnNotFoundWhenSemesterOfferingsRequestedForNonExistentSemester() throws Exception {
        when(getSemesterCourseOfferingsQueryHandler.handle(new GetSemesterCourseOfferingsQuery(SEMESTER_ID)))
                .thenThrow(new SemesterNotFoundException(SEMESTER_ID));

        mockMvc.perform(get(OFFERINGS_URL, SEMESTER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with empty offerings list when semester has no offerings")
    void shouldReturnOkWithEmptyOfferingsListWhenSemesterHasNoOfferings() throws Exception {
        GetSemesterCourseOfferingsResponse response = new GetSemesterCourseOfferingsResponse(
                SEMESTER_ID, TITLE, List.of()
        );

        when(getSemesterCourseOfferingsQueryHandler.handle(new GetSemesterCourseOfferingsQuery(SEMESTER_ID)))
                .thenReturn(response);

        mockMvc.perform(get(OFFERINGS_URL, SEMESTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterId").value(SEMESTER_ID))
                .andExpect(jsonPath("$.offerings").isArray())
                .andExpect(jsonPath("$.offerings").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when semester json payload is malformed")
    void shouldReturnBadRequestWhenSemesterJsonIsMalformed() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MALFORMED_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when semester path parameter is not numeric")
    void shouldReturnBadRequestWhenSemesterIdIsNotNumeric() throws Exception {
        mockMvc.perform(get(NON_NUMERIC_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
