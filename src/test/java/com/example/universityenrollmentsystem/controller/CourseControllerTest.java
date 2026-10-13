package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidCourseStateException;
import com.example.universityenrollmentsystem.service.courses.archive.ArchiveCourseCommand;
import com.example.universityenrollmentsystem.service.courses.archive.ArchiveCourseCommandHandler;
import com.example.universityenrollmentsystem.service.courses.create.CreateCourseCommand;
import com.example.universityenrollmentsystem.service.courses.create.CreateCourseCommandHandler;
import com.example.universityenrollmentsystem.service.courses.credits.UpdateCourseCreditsCommandHandler;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsQuery;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsResponse;
import com.example.universityenrollmentsystem.service.courses.prerequisites.UpdateCoursePrerequisitesCommandHandler;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsQuery;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsQueryHandler;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsResponse;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
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

import java.util.Collections;
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
class CourseControllerTest {

    private static final Long COURSE_ID = 1L;
    private static final String COURSE_NAME = "Advanced Programming";
    private static final String COURSE_CODE = "AP101";
    private static final int CREDITS = 4;
    private static final String BASE_URL = "/api/v1/courses";
    private static final String COURSE_URL = "/api/v1/courses/{id}";
    private static final String CREDITS_URL = "/api/v1/courses/{id}/credits";
    private static final String PREREQUISITES_URL = "/api/v1/courses/{id}/prerequisites";
    private static final String STATISTICS_URL = "/api/v1/courses/{id}/statistics";

    private static final String BLANK_NAME_ERROR = "Course name cannot be blank";
    private static final String MAJOR_NOT_FOUND_ERROR = "Major with specified ID does not exist";
    private static final String CREDITS_OUT_OF_BOUNDS_ERROR = "Credits must be between 1 and 4";
    private static final String SELF_PREREQUISITE_ERROR = "A course cannot be a prerequisite of itself";

    @Mock
    private CreateCourseCommandHandler createCourseCommandHandler;

    @Mock
    private ArchiveCourseCommandHandler archiveCourseCommandHandler;

    @Mock
    private UpdateCourseCreditsCommandHandler updateCourseCreditsCommandHandler;

    @Mock
    private UpdateCoursePrerequisitesCommandHandler updateCoursePrerequisitesCommandHandler;

    @Mock
    private GetCourseDetailsQueryHandler getCourseDetailsQueryHandler;

    @Mock
    private GetCourseStatisticsQueryHandler getCourseStatisticsQueryHandler;

    @InjectMocks
    private CourseController courseController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with Location header when creating course successfully")
    void shouldCreateCourseSuccessfully() throws Exception {
        when(createCourseCommandHandler.handle(any(CreateCourseCommand.class))).thenReturn(COURSE_ID);

        String jsonPayload = """
                {
                    "name": "Advanced Programming",
                    "courseCode": "AP101",
                    "credits": 4
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/courses/" + COURSE_ID))
                .andExpect(jsonPath("$").value(COURSE_ID));

        verify(createCourseCommandHandler).handle(any(CreateCourseCommand.class));
    }

    @Test
    @DisplayName("Should return 409 Conflict when course code already exists")
    void shouldReturnConflictWhenCourseCodeAlreadyExists() throws Exception {
        when(createCourseCommandHandler.handle(any(CreateCourseCommand.class)))
                .thenThrow(new CourseAlreadyExistsException(COURSE_CODE));

        String jsonPayload = """
                {
                    "name": "Advanced Programming",
                    "courseCode": "AP101",
                    "credits": 4
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when major id does not exist on create course")
    void shouldReturnBadRequestWhenMajorDoesNotExistOnCreateCourse() throws Exception {
        when(createCourseCommandHandler.handle(any(CreateCourseCommand.class)))
                .thenThrow(new CommandValidationException(MAJOR_NOT_FOUND_ERROR));

        String jsonPayload = """
                {
                    "name": "Advanced Programming",
                    "courseCode": "AP101",
                    "credits": 4,
                    "majorIds": [999]
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MAJOR_NOT_FOUND_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when credits are out of bounds on create course")
    void shouldReturnBadRequestWhenCreditsOutOfBoundsOnCreateCourse() throws Exception {
        when(createCourseCommandHandler.handle(any(CreateCourseCommand.class)))
                .thenThrow(new CommandValidationException(CREDITS_OUT_OF_BOUNDS_ERROR));

        String jsonPayload = """
                {
                    "name": "Advanced Programming",
                    "courseCode": "AP101",
                    "credits": 0
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CREDITS_OUT_OF_BOUNDS_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when create course command validation fails with blank name")
    void shouldReturnBadRequestWhenCreateCourseValidationFails() throws Exception {
        when(createCourseCommandHandler.handle(any(CreateCourseCommand.class)))
                .thenThrow(new CommandValidationException(BLANK_NAME_ERROR));

        String jsonPayload = """
                {
                    "name": "",
                    "courseCode": "AP101",
                    "credits": 4
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(BLANK_NAME_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when JSON body is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed: json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }

    @Test
    @DisplayName("Should return 204 No Content when archiving course successfully")
    void shouldArchiveCourseSuccessfully() throws Exception {
        mockMvc.perform(delete(COURSE_URL, COURSE_ID))
                .andExpect(status().isNoContent());

        verify(archiveCourseCommandHandler).handle(new ArchiveCourseCommand(COURSE_ID));
    }

    @Test
    @DisplayName("Should return 404 Not Found when archiving non-existent course")
    void shouldReturnNotFoundWhenArchivingNonExistentCourse() throws Exception {
        doThrow(new CourseNotFoundException(COURSE_ID))
                .when(archiveCourseCommandHandler).handle(new ArchiveCourseCommand(COURSE_ID));

        mockMvc.perform(delete(COURSE_URL, COURSE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when course ID in URL is non-numeric")
    void shouldReturnBadRequestWhenCourseIdIsNonNumeric() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when updating course credits successfully")
    void shouldUpdateCourseCreditsSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "credits": 3
                }
                """;

        mockMvc.perform(put(CREDITS_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(updateCourseCreditsCommandHandler).handle(any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update credits validation fails")
    void shouldReturnBadRequestWhenUpdateCreditsValidationFails() throws Exception {
        doThrow(new CommandValidationException(CREDITS_OUT_OF_BOUNDS_ERROR))
                .when(updateCourseCreditsCommandHandler).handle(any());

        String jsonPayload = """
                {
                    "credits": 5
                }
                """;

        mockMvc.perform(put(CREDITS_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CREDITS_OUT_OF_BOUNDS_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating credits for non-existent course")
    void shouldReturnNotFoundWhenUpdatingCreditsForNonExistentCourse() throws Exception {
        doThrow(new CourseNotFoundException(COURSE_ID))
                .when(updateCourseCreditsCommandHandler).handle(any());

        String jsonPayload = """
                {
                    "credits": 3
                }
                """;

        mockMvc.perform(put(CREDITS_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when updating course prerequisites")
    void shouldUpdateCoursePrerequisitesSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "prerequisiteIds": [2, 3]
                }
                """;

        mockMvc.perform(put(PREREQUISITES_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(updateCoursePrerequisitesCommandHandler).handle(any());
    }

    @Test
    @DisplayName("Should return 204 No Content when clearing prerequisites with empty list")
    void shouldClearPrerequisitesSuccessfullyWithEmptyList() throws Exception {
        String jsonPayload = """
                {
                    "prerequisiteIds": []
                }
                """;

        mockMvc.perform(put(PREREQUISITES_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(updateCoursePrerequisitesCommandHandler).handle(any());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when setting course as its own prerequisite")
    void shouldReturnUnprocessableEntityWhenSettingSelfAsPrerequisite() throws Exception {
        doThrow(new InvalidCourseStateException(SELF_PREREQUISITE_ERROR))
                .when(updateCoursePrerequisitesCommandHandler).handle(any());

        String jsonPayload = """
                {
                    "prerequisiteIds": [1]
                }
                """;

        mockMvc.perform(put(PREREQUISITES_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(SELF_PREREQUISITE_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating prerequisites for non-existent course")
    void shouldReturnNotFoundWhenUpdatingPrerequisitesForNonExistentCourse() throws Exception {
        doThrow(new CourseNotFoundException(COURSE_ID))
                .when(updateCoursePrerequisitesCommandHandler).handle(any());

        String jsonPayload = """
                {
                    "prerequisiteIds": [2, 3]
                }
                """;

        mockMvc.perform(put(PREREQUISITES_URL, COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with course details response")
    void shouldGetCourseDetailsSuccessfully() throws Exception {
        GetCourseDetailsResponse detailsResponse = new GetCourseDetailsResponse(
                COURSE_ID,
                COURSE_NAME,
                COURSE_CODE,
                CREDITS,
                false,
                Collections.emptyList(),
                List.of("Computer Engineering")
        );

        when(getCourseDetailsQueryHandler.handle(new GetCourseDetailsQuery(COURSE_ID)))
                .thenReturn(detailsResponse);

        mockMvc.perform(get(COURSE_URL, COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(COURSE_ID))
                .andExpect(jsonPath("$.name").value(COURSE_NAME))
                .andExpect(jsonPath("$.courseCode").value(COURSE_CODE))
                .andExpect(jsonPath("$.credits").value(CREDITS))
                .andExpect(jsonPath("$.majors[0]").value("Computer Engineering"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when querying details for non-existent course")
    void shouldReturnNotFoundWhenQueryingNonExistentCourse() throws Exception {
        when(getCourseDetailsQueryHandler.handle(new GetCourseDetailsQuery(COURSE_ID)))
                .thenThrow(new CourseNotFoundException(COURSE_ID));

        mockMvc.perform(get(COURSE_URL, COURSE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with course statistics response")
    void shouldGetCourseStatisticsSuccessfully() throws Exception {
        GetCourseStatisticsResponse.OverallCourseStatistics overall =
                new GetCourseStatisticsResponse.OverallCourseStatistics(
                        COURSE_ID, COURSE_NAME, COURSE_CODE, 2, 20, 5, 2, 16.5
                );

        GetCourseStatisticsResponse statisticsResponse =
                new GetCourseStatisticsResponse(overall, Collections.emptyList());

        when(getCourseStatisticsQueryHandler.handle(new GetCourseStatisticsQuery(COURSE_ID)))
                .thenReturn(statisticsResponse);

        mockMvc.perform(get(STATISTICS_URL, COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall.courseId").value(COURSE_ID))
                .andExpect(jsonPath("$.overall.courseName").value(COURSE_NAME))
                .andExpect(jsonPath("$.overall.averageGrade").value(16.5));
    }

    @Test
    @DisplayName("Should return 200 OK with zero average when course has no offerings or grades")
    void shouldReturnOkWithZeroAverageWhenCourseHasNoOfferings() throws Exception {
        GetCourseStatisticsResponse.OverallCourseStatistics overall =
                new GetCourseStatisticsResponse.OverallCourseStatistics(
                        COURSE_ID, COURSE_NAME, COURSE_CODE, 0, 0, 0, 0, 0.0
                );

        GetCourseStatisticsResponse statisticsResponse =
                new GetCourseStatisticsResponse(overall, Collections.emptyList());

        when(getCourseStatisticsQueryHandler.handle(new GetCourseStatisticsQuery(COURSE_ID)))
                .thenReturn(statisticsResponse);

        mockMvc.perform(get(STATISTICS_URL, COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall.averageGrade").value(0.0))
                .andExpect(jsonPath("$.overall.totalStudentsPassed").value(0))
                .andExpect(jsonPath("$.overall.totalStudentsFailed").value(0))
                .andExpect(jsonPath("$.overall.totalStudentsInProgress").value(0))
                .andExpect(jsonPath("$.byMajor").isEmpty());
    }

    @Test
    @DisplayName("Should return 200 OK with zero average when all students are in progress without grades")
    void shouldReturnOkWithZeroAverageWhenAllStudentsInProgress() throws Exception {
        GetCourseStatisticsResponse.OverallCourseStatistics overall =
                new GetCourseStatisticsResponse.OverallCourseStatistics(
                        COURSE_ID, COURSE_NAME, COURSE_CODE, 1, 0, 0, 10, 0.0
                );

        GetCourseStatisticsResponse statisticsResponse =
                new GetCourseStatisticsResponse(overall, Collections.emptyList());

        when(getCourseStatisticsQueryHandler.handle(new GetCourseStatisticsQuery(COURSE_ID)))
                .thenReturn(statisticsResponse);

        mockMvc.perform(get(STATISTICS_URL, COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall.totalStudentsInProgress").value(10))
                .andExpect(jsonPath("$.overall.averageGrade").value(0.0));
    }

    @Test
    @DisplayName("Should return 404 Not Found when querying statistics for non-existent course")
    void shouldReturnNotFoundWhenQueryingStatisticsForNonExistentCourse() throws Exception {
        when(getCourseStatisticsQueryHandler.handle(new GetCourseStatisticsQuery(COURSE_ID)))
                .thenThrow(new CourseNotFoundException(COURSE_ID));

        mockMvc.perform(get(STATISTICS_URL, COURSE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
