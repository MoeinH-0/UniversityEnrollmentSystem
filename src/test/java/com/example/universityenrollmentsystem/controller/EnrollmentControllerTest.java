package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.domain.exception.rule.CourseNotInMajorException;
import com.example.universityenrollmentsystem.domain.exception.rule.DuplicateEnrollmentException;
import com.example.universityenrollmentsystem.domain.exception.rule.GuestPolicyViolationException;
import com.example.universityenrollmentsystem.domain.exception.rule.MaxCreditsExceededException;
import com.example.universityenrollmentsystem.domain.exception.rule.OutsideMajorLimitExceededException;
import com.example.universityenrollmentsystem.domain.exception.rule.PrerequisiteNotMetException;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.service.enrollments.drop.DropCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.enrollments.drop.DropCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesQuery;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesQueryHandler;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesResponse;
import com.example.universityenrollmentsystem.service.enrollments.submit.SubmitEnrollmentCommand;
import com.example.universityenrollmentsystem.service.enrollments.submit.SubmitEnrollmentCommandHandler;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseOfferingFullException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long SEMESTER_ID = 10L;
    private static final Long OFFERING_ID = 100L;
    private static final Long COURSE_ID = 10L;
    private static final String COURSE_NAME = "Calculus";
    private static final String COURSE_CODE = "MATH101";
    private static final int CREDITS = 3;
    private static final int CAPACITY = 30;
    private static final int MAX_CREDITS_REGULAR = 24;
    private static final int MAX_CREDITS_PROBATION = 14;
    private static final int MAX_CREDITS_HONORS = 28;
    private static final int MAX_CREDITS_GUEST = 20;
    private static final int HONORS_OUTSIDE_LIMIT = 1;

    private static final String PLAN_REGULAR = "REGULAR";
    private static final String PLAN_PROBATION = "PROBATION";
    private static final String PLAN_HONORS = "HONORS";
    private static final String PLAN_GUEST = "GUEST";

    private static final String PROFESSOR = "Dr. Alavi";
    private static final String SECTION = "01";
    private static final String CLASS_TIME = "Sun 08:00";
    private static final String DUPLICATE_ERROR = "Student already enrolled";
    private static final String ALREADY_PASSED_ERROR = "Student has already passed this course";
    private static final String INACTIVE_STUDENT_ERROR = "Student is not in active status";
    private static final String DELETED_OFFERING_ERROR = "Cannot enroll in a cancelled offering";
    private static final String NOT_ENROLLED_DROP_ERROR = "Cannot drop course that was never taken";
    private static final String COMPLETED_DROP_ERROR = "Cannot drop course that is already graded";
    private static final String VALIDATION_ERROR = "Student ID cannot be null";

    private static final String BASE_URL = "/api/v1/enrollments";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/{courseOfferingId}/students/{studentId}";
    private static final String ELIGIBLE_URL = "/api/v1/enrollments/eligible";

    @Mock
    private SubmitEnrollmentCommandHandler submitEnrollmentCommandHandler;

    @Mock
    private DropCourseOfferingCommandHandler dropCourseOfferingCommandHandler;

    @Mock
    private GetEligibleCoursesQueryHandler getEligibleCoursesQueryHandler;

    @InjectMocks
    private EnrollmentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK when submitting enrollment successfully")
    void shouldSubmitEnrollmentSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        verify(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when submit enrollment validation fails")
    void shouldReturnBadRequestWhenSubmitValidationFails() throws Exception {
        doThrow(new CommandValidationException(VALIDATION_ERROR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": null,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when student is inactive on submit enrollment")
    void shouldReturnBadRequestWhenStudentIsInactiveOnSubmit() throws Exception {
        doThrow(new CommandValidationException(INACTIVE_STUDENT_ERROR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INACTIVE_STUDENT_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when course offering is cancelled on submit enrollment")
    void shouldReturnBadRequestWhenOfferingIsCancelledOnSubmit() throws Exception {
        doThrow(new CommandValidationException(DELETED_OFFERING_ERROR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(DELETED_OFFERING_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when student does not exist on enrollment submit")
    void shouldReturnNotFoundWhenStudentDoesNotExistOnSubmit() throws Exception {
        doThrow(new StudentNotFoundException(STUDENT_ID))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when course offering does not exist on enrollment submit")
    void shouldReturnNotFoundWhenOfferingDoesNotExistOnSubmit() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 409 Conflict when course offering capacity is full")
    void shouldReturnConflictWhenOfferingIsFull() throws Exception {
        doThrow(new CourseOfferingFullException(OFFERING_ID))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 409 Conflict when student is currently enrolled in course")
    void shouldReturnConflictWhenEnrollmentIsDuplicate() throws Exception {
        doThrow(new DuplicateEnrollmentException(DUPLICATE_ERROR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(DUPLICATE_ERROR));
    }

    @Test
    @DisplayName("Should return 409 Conflict when student has already passed the course")
    void shouldReturnConflictWhenStudentHasAlreadyPassedCourse() throws Exception {
        doThrow(new DuplicateEnrollmentException(ALREADY_PASSED_ERROR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ALREADY_PASSED_ERROR));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when regular credit limit is exceeded")
    void shouldReturnUnprocessableEntityWhenCreditLimitExceeded() throws Exception {
        doThrow(new MaxCreditsExceededException(MAX_CREDITS_REGULAR, PLAN_REGULAR))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when probation credit limit is exceeded")
    void shouldReturnUnprocessableEntityWhenProbationCreditLimitExceeded() throws Exception {
        doThrow(new MaxCreditsExceededException(MAX_CREDITS_PROBATION, PLAN_PROBATION))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when honors credit limit is exceeded")
    void shouldReturnUnprocessableEntityWhenHonorsCreditLimitExceeded() throws Exception {
        doThrow(new MaxCreditsExceededException(MAX_CREDITS_HONORS, PLAN_HONORS))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when honors student takes second course outside major")
    void shouldReturnUnprocessableEntityWhenHonorsExceedsOutsideMajorLimit() throws Exception {
        doThrow(new OutsideMajorLimitExceededException(HONORS_OUTSIDE_LIMIT))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when guest credit limit is exceeded")
    void shouldReturnUnprocessableEntityWhenGuestCreditLimitExceeded() throws Exception {
        doThrow(new MaxCreditsExceededException(MAX_CREDITS_GUEST, PLAN_GUEST))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when guest student selects disallowed course")
    void shouldReturnUnprocessableEntityWhenGuestSelectsDisallowedCourse() throws Exception {
        doThrow(new GuestPolicyViolationException())
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when prerequisite is not met")
    void shouldReturnUnprocessableEntityWhenPrerequisiteNotMet() throws Exception {
        doThrow(new PrerequisiteNotMetException(COURSE_NAME))
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when course is not in student major")
    void shouldReturnUnprocessableEntityWhenCourseNotInMajor() throws Exception {
        doThrow(new CourseNotInMajorException())
                .when(submitEnrollmentCommandHandler).handle(any(SubmitEnrollmentCommand.class));

        String jsonPayload = """
                {
                    "studentId": 1,
                    "courseOfferingId": 100
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when dropping course offering")
    void shouldDropCourseOfferingSuccessfully() throws Exception {
        mockMvc.perform(delete(DROP_URL, OFFERING_ID, STUDENT_ID))
                .andExpect(status().isNoContent());

        verify(dropCourseOfferingCommandHandler).handle(new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when dropping course not taken by student")
    void shouldReturnUnprocessableEntityWhenDroppingCourseNotTaken() throws Exception {
        doThrow(new InvalidEnrollmentStateException(NOT_ENROLLED_DROP_ERROR))
                .when(dropCourseOfferingCommandHandler).handle(new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID));

        mockMvc.perform(delete(DROP_URL, OFFERING_ID, STUDENT_ID))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(NOT_ENROLLED_DROP_ERROR));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when dropping completed course")
    void shouldReturnUnprocessableEntityWhenDroppingCompletedCourse() throws Exception {
        doThrow(new InvalidEnrollmentStateException(COMPLETED_DROP_ERROR))
                .when(dropCourseOfferingCommandHandler).handle(new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID));

        mockMvc.perform(delete(DROP_URL, OFFERING_ID, STUDENT_ID))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(COMPLETED_DROP_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when dropping offering for non-existent student")
    void shouldReturnNotFoundWhenStudentDoesNotExistOnDrop() throws Exception {
        doThrow(new StudentNotFoundException(STUDENT_ID))
                .when(dropCourseOfferingCommandHandler).handle(new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID));

        mockMvc.perform(delete(DROP_URL, OFFERING_ID, STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when dropping non-existent course offering")
    void shouldReturnNotFoundWhenOfferingDoesNotExistOnDrop() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(dropCourseOfferingCommandHandler).handle(new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID));

        mockMvc.perform(delete(DROP_URL, OFFERING_ID, STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with eligible courses for student")
    void shouldGetEligibleCoursesSuccessfully() throws Exception {
        GetEligibleCoursesResponse.EligibleCourseOfferingDto dto =
                new GetEligibleCoursesResponse.EligibleCourseOfferingDto(
                        OFFERING_ID, COURSE_ID, COURSE_NAME, COURSE_CODE, CREDITS,
                        PROFESSOR, SECTION, CAPACITY, CLASS_TIME,
                        LocalDate.of(2027, 1, 20), true
                );

        GetEligibleCoursesResponse response = new GetEligibleCoursesResponse(
                STUDENT_ID, SEMESTER_ID, List.of(dto)
        );

        when(getEligibleCoursesQueryHandler.handle(new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID)))
                .thenReturn(response);

        mockMvc.perform(get(ELIGIBLE_URL)
                        .param("studentId", STUDENT_ID.toString())
                        .param("semesterId", SEMESTER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.eligibleOfferings[0].courseName").value(COURSE_NAME));
    }

    @Test
    @DisplayName("Should return 200 OK with empty list when all courses are filtered out")
    void shouldGetEmptyEligibleCoursesWhenAllFiltered() throws Exception {
        GetEligibleCoursesResponse response = new GetEligibleCoursesResponse(
                STUDENT_ID, SEMESTER_ID, Collections.emptyList()
        );

        when(getEligibleCoursesQueryHandler.handle(new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID)))
                .thenReturn(response);

        mockMvc.perform(get(ELIGIBLE_URL)
                        .param("studentId", STUDENT_ID.toString())
                        .param("semesterId", SEMESTER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleOfferings").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 Not Found when student does not exist on getting eligible courses")
    void shouldReturnNotFoundWhenStudentDoesNotExistOnEligibleCourses() throws Exception {
        when(getEligibleCoursesQueryHandler.handle(new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID)))
                .thenThrow(new StudentNotFoundException(STUDENT_ID));

        mockMvc.perform(get(ELIGIBLE_URL)
                        .param("studentId", STUDENT_ID.toString())
                        .param("semesterId", SEMESTER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when semester does not exist on getting eligible courses")
    void shouldReturnNotFoundWhenSemesterDoesNotExistOnEligibleCourses() throws Exception {
        when(getEligibleCoursesQueryHandler.handle(new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID)))
                .thenThrow(new SemesterNotFoundException(SEMESTER_ID));

        mockMvc.perform(get(ELIGIBLE_URL)
                        .param("studentId", STUDENT_ID.toString())
                        .param("semesterId", SEMESTER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
