package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.service.exceptions.conflict.StudentAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import com.example.universityenrollmentsystem.service.students.contact.UpdateStudentContactInfoCommand;
import com.example.universityenrollmentsystem.service.students.contact.UpdateStudentContactInfoCommandHandler;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsQuery;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsResponse;
import com.example.universityenrollmentsystem.service.students.major.ChangeStudentMajorCommand;
import com.example.universityenrollmentsystem.service.students.major.ChangeStudentMajorCommandHandler;
import com.example.universityenrollmentsystem.service.students.registration.RegisterStudentCommand;
import com.example.universityenrollmentsystem.service.students.registration.RegisterStudentCommandHandler;
import com.example.universityenrollmentsystem.service.students.status.ChangeStudentStatusCommand;
import com.example.universityenrollmentsystem.service.students.status.ChangeStudentStatusCommandHandler;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptQuery;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptQueryHandler;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long MAJOR_ID = 2L;
    private static final String FULL_NAME = "Kasra Alavi";
    private static final String STUDENT_NUMBER = "401123456";
    private static final String MAJOR_NAME = "Computer Engineering";
    private static final String EMAIL = "kasra@uni.edu";
    private static final String PHONE = "09121112233";
    private static final String COURSE_NAME = "Algorithms";
    private static final String COURSE_CODE = "CS302";
    private static final int CREDITS = 3;
    private static final double GRADE = 18.5;
    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final String VALIDATION_ERROR = "Student name cannot be blank";
    private static final String EMAIL_ERROR = "Invalid email format";

    private static final String BASE_URL = "/api/v1/students";
    private static final String STUDENT_URL = "/api/v1/students/{id}";
    private static final String STATUS_URL = "/api/v1/students/{id}/status";
    private static final String MAJOR_URL = "/api/v1/students/{id}/major";
    private static final String CONTACT_URL = "/api/v1/students/{id}/contact";
    private static final String TRANSCRIPT_URL = "/api/v1/students/{id}/transcript";
    private static final String NON_NUMERIC_URL = "/api/v1/students/invalid-id";
    private static final String MALFORMED_JSON = "{\"fullName\": \"Kasra\", unclosed";
    private static final String INVALID_STATUS_JSON = "{\"status\": \"INVALID_STATUS\"}";
    private static final String EMPTY_CONTACT_JSON = "{\"email\": null, \"phoneNumber\": null}";
    private static final double ZERO_GPA = 0.0;
    private static final int ZERO_CREDITS = 0;
    private static final double PASS_GRADE = 18.0;
    private static final double FAIL_GRADE = 6.0;
    private static final double WEIGHTED_GPA = 12.0;
    private static final int PASSED_CREDITS = 3;
    private static final String FAILED_COURSE_NAME = "Physics";
    private static final String FAILED_COURSE_CODE = "PHYS101";

    @Mock
    private RegisterStudentCommandHandler registerStudentCommandHandler;

    @Mock
    private ChangeStudentStatusCommandHandler changeStudentStatusCommandHandler;

    @Mock
    private ChangeStudentMajorCommandHandler changeStudentMajorCommandHandler;

    @Mock
    private UpdateStudentContactInfoCommandHandler updateStudentContactInfoCommandHandler;

    @Mock
    private GetStudentDetailsQueryHandler getStudentDetailsQueryHandler;

    @Mock
    private GetStudentTranscriptQueryHandler getStudentTranscriptQueryHandler;

    @InjectMocks
    private StudentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with Location header when registering student")
    void shouldRegisterStudentSuccessfully() throws Exception {
        when(registerStudentCommandHandler.handle(any(RegisterStudentCommand.class))).thenReturn(STUDENT_ID);

        String jsonPayload = """
                {
                    "fullName": "Kasra Alavi",
                    "studentNumber": "401123456",
                    "majorId": 1,
                    "planType": "REGULAR"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/students/" + STUDENT_ID))
                .andExpect(jsonPath("$").value(STUDENT_ID));

        verify(registerStudentCommandHandler).handle(any(RegisterStudentCommand.class));
    }

    @Test
    @DisplayName("Should return 409 Conflict when student number already exists")
    void shouldReturnConflictWhenStudentAlreadyExists() throws Exception {
        when(registerStudentCommandHandler.handle(any(RegisterStudentCommand.class)))
                .thenThrow(new StudentAlreadyExistsException(STUDENT_NUMBER));

        String jsonPayload = """
                {
                    "fullName": "Kasra Alavi",
                    "studentNumber": "401123456",
                    "majorId": 1,
                    "planType": "REGULAR"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when register student validation fails")
    void shouldReturnBadRequestWhenRegisterValidationFails() throws Exception {
        when(registerStudentCommandHandler.handle(any(RegisterStudentCommand.class)))
                .thenThrow(new CommandValidationException(VALIDATION_ERROR));

        String jsonPayload = """
                {
                    "fullName": "",
                    "studentNumber": "401123456",
                    "majorId": 1,
                    "planType": "REGULAR"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when changing student status")
    void shouldChangeStudentStatusSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "status": "WITHDRAWN"
                }
                """;

        mockMvc.perform(put(STATUS_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(changeStudentStatusCommandHandler).handle(any(ChangeStudentStatusCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when changing status for non-existent student")
    void shouldReturnNotFoundWhenChangingStatusForNonExistentStudent() throws Exception {
        doThrow(new StudentNotFoundException(STUDENT_ID))
                .when(changeStudentStatusCommandHandler).handle(any(ChangeStudentStatusCommand.class));

        String jsonPayload = """
                {
                    "status": "WITHDRAWN"
                }
                """;

        mockMvc.perform(put(STATUS_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when changing student major")
    void shouldChangeStudentMajorSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "majorId": 2
                }
                """;

        mockMvc.perform(put(MAJOR_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(changeStudentMajorCommandHandler).handle(any(ChangeStudentMajorCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when changing major for non-existent student")
    void shouldReturnNotFoundWhenChangingMajorForNonExistentStudent() throws Exception {
        doThrow(new StudentNotFoundException(STUDENT_ID))
                .when(changeStudentMajorCommandHandler).handle(any(ChangeStudentMajorCommand.class));

        String jsonPayload = """
                {
                    "majorId": 2
                }
                """;

        mockMvc.perform(put(MAJOR_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when target major does not exist on change major")
    void shouldReturnNotFoundWhenMajorDoesNotExistOnChangeMajor() throws Exception {
        doThrow(new MajorNotFoundException(MAJOR_ID))
                .when(changeStudentMajorCommandHandler).handle(any(ChangeStudentMajorCommand.class));

        String jsonPayload = """
                {
                    "majorId": 2
                }
                """;

        mockMvc.perform(put(MAJOR_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when updating contact info")
    void shouldUpdateContactInfoSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "email": "kasra@uni.edu",
                    "phoneNumber": "09121112233"
                }
                """;

        mockMvc.perform(put(CONTACT_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(updateStudentContactInfoCommandHandler).handle(any(UpdateStudentContactInfoCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating contact info for non-existent student")
    void shouldReturnNotFoundWhenUpdatingContactForNonExistentStudent() throws Exception {
        doThrow(new StudentNotFoundException(STUDENT_ID))
                .when(updateStudentContactInfoCommandHandler).handle(any(UpdateStudentContactInfoCommand.class));

        String jsonPayload = """
                {
                    "email": "kasra@uni.edu",
                    "phoneNumber": "09121112233"
                }
                """;

        mockMvc.perform(put(CONTACT_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update contact info validation fails")
    void shouldReturnBadRequestWhenUpdateContactValidationFails() throws Exception {
        doThrow(new CommandValidationException(EMAIL_ERROR))
                .when(updateStudentContactInfoCommandHandler).handle(any(UpdateStudentContactInfoCommand.class));

        String jsonPayload = """
                {
                    "email": "invalid-email",
                    "phoneNumber": "09121112233"
                }
                """;

        mockMvc.perform(put(CONTACT_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(EMAIL_ERROR));
    }

    @Test
    @DisplayName("Should return 200 OK with student details")
    void shouldGetStudentDetailsSuccessfully() throws Exception {
        GetStudentDetailsResponse response = new GetStudentDetailsResponse(
                STUDENT_ID, FULL_NAME, STUDENT_NUMBER, 1L, MAJOR_NAME,
                EnrollmentPlanType.REGULAR, StudentStatus.ACTIVE, EMAIL, PHONE
        );

        when(getStudentDetailsQueryHandler.handle(new GetStudentDetailsQuery(STUDENT_ID))).thenReturn(response);

        mockMvc.perform(get(STUDENT_URL, STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.studentNumber").value(STUDENT_NUMBER));
    }

    @Test
    @DisplayName("Should return 404 Not Found when student does not exist")
    void shouldReturnNotFoundWhenStudentDoesNotExist() throws Exception {
        when(getStudentDetailsQueryHandler.handle(new GetStudentDetailsQuery(STUDENT_ID)))
                .thenThrow(new StudentNotFoundException(STUDENT_ID));

        mockMvc.perform(get(STUDENT_URL, STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with student transcript")
    void shouldGetStudentTranscriptSuccessfully() throws Exception {
        GetStudentTranscriptResponse.CourseGradeDto courseDto =
                new GetStudentTranscriptResponse.CourseGradeDto(
                        10L, COURSE_NAME, COURSE_CODE, CREDITS, GRADE, CourseStatus.PASSED
                );

        GetStudentTranscriptResponse.SemesterTranscriptDto semesterDto =
                new GetStudentTranscriptResponse.SemesterTranscriptDto(
                        1L, SEMESTER_TITLE, GRADE, CREDITS, List.of(courseDto)
                );

        GetStudentTranscriptResponse response = new GetStudentTranscriptResponse(
                STUDENT_ID, FULL_NAME, STUDENT_NUMBER, MAJOR_NAME,
                GRADE, CREDITS, List.of(semesterDto)
        );

        when(getStudentTranscriptQueryHandler.handle(new GetStudentTranscriptQuery(STUDENT_ID)))
                .thenReturn(response);

        mockMvc.perform(get(TRANSCRIPT_URL, STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.gpa").value(GRADE))
                .andExpect(jsonPath("$.semesters[0].courses[0].grade").value(GRADE));
    }

    @Test
    @DisplayName("Should return 404 Not Found when transcript requested for non-existent student")
    void shouldReturnNotFoundWhenTranscriptRequestedForNonExistentStudent() throws Exception {
        when(getStudentTranscriptQueryHandler.handle(new GetStudentTranscriptQuery(STUDENT_ID)))
                .thenThrow(new StudentNotFoundException(STUDENT_ID));

        mockMvc.perform(get(TRANSCRIPT_URL, STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when student status enum value is invalid")
    void shouldReturnBadRequestWhenStudentStatusEnumValueIsInvalid() throws Exception {
        mockMvc.perform(put(STATUS_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_STATUS_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when clearing contact info with null or empty fields")
    void shouldClearContactInfoWhenNullOrEmptyFieldsProvided() throws Exception {
        mockMvc.perform(put(CONTACT_URL, STUDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMPTY_CONTACT_JSON))
                .andExpect(status().isNoContent());

        verify(updateStudentContactInfoCommandHandler).handle(any(UpdateStudentContactInfoCommand.class));
    }

    @Test
    @DisplayName("Should return 200 OK with zero GPA for new student without completed courses")
    void shouldReturnZeroGpaForNewStudentWithNoCourses() throws Exception {
        GetStudentTranscriptResponse response = new GetStudentTranscriptResponse(
                STUDENT_ID, FULL_NAME, STUDENT_NUMBER, MAJOR_NAME,
                ZERO_GPA, ZERO_CREDITS, List.of()
        );

        when(getStudentTranscriptQueryHandler.handle(new GetStudentTranscriptQuery(STUDENT_ID)))
                .thenReturn(response);

        mockMvc.perform(get(TRANSCRIPT_URL, STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(ZERO_GPA))
                .andExpect(jsonPath("$.totalPassedCredits").value(ZERO_CREDITS))
                .andExpect(jsonPath("$.semesters").isArray())
                .andExpect(jsonPath("$.semesters").isEmpty());
    }

    @Test
    @DisplayName("Should return 200 OK with weighted GPA including failed courses but excluding them from passed credits")
    void shouldCalculateWeightedGpaIncludingFailedCoursesWhileExcludingFromPassedCredits() throws Exception {
        GetStudentTranscriptResponse.CourseGradeDto passedCourseDto =
                new GetStudentTranscriptResponse.CourseGradeDto(
                        10L, COURSE_NAME, COURSE_CODE, CREDITS, PASS_GRADE, CourseStatus.PASSED
                );

        GetStudentTranscriptResponse.CourseGradeDto failedCourseDto =
                new GetStudentTranscriptResponse.CourseGradeDto(
                        11L, FAILED_COURSE_NAME, FAILED_COURSE_CODE, CREDITS, FAIL_GRADE, CourseStatus.FAILED
                );

        GetStudentTranscriptResponse.SemesterTranscriptDto semesterDto =
                new GetStudentTranscriptResponse.SemesterTranscriptDto(
                        1L, SEMESTER_TITLE, WEIGHTED_GPA, PASSED_CREDITS, List.of(passedCourseDto, failedCourseDto)
                );

        GetStudentTranscriptResponse response = new GetStudentTranscriptResponse(
                STUDENT_ID, FULL_NAME, STUDENT_NUMBER, MAJOR_NAME,
                WEIGHTED_GPA, PASSED_CREDITS, List.of(semesterDto)
        );

        when(getStudentTranscriptQueryHandler.handle(new GetStudentTranscriptQuery(STUDENT_ID)))
                .thenReturn(response);

        mockMvc.perform(get(TRANSCRIPT_URL, STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(WEIGHTED_GPA))
                .andExpect(jsonPath("$.totalPassedCredits").value(PASSED_CREDITS))
                .andExpect(jsonPath("$.semesters[0].courses[0].status").value("PASSED"))
                .andExpect(jsonPath("$.semesters[0].courses[1].status").value("FAILED"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when register student json payload is malformed")
    void shouldReturnBadRequestWhenRegisterStudentJsonIsMalformed() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MALFORMED_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when student path parameter is not numeric")
    void shouldReturnBadRequestWhenStudentIdIsNotNumeric() throws Exception {
        mockMvc.perform(get(NON_NUMERIC_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
