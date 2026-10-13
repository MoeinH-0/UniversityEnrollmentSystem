package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.service.courseOfferings.cancel.CancelCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.cancel.CancelCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.capacity.ChangeCourseOfferingCapacityCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.capacity.ChangeCourseOfferingCapacityCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.create.CreateCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.create.CreateCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsQuery;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsResponse;
import com.example.universityenrollmentsystem.service.courseOfferings.exam.RescheduleCourseOfferingExamCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.exam.RescheduleCourseOfferingExamCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.grade.SubmitStudentGradeCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.grade.SubmitStudentGradeCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy.ChangeCourseOfferingGuestPolicyCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy.ChangeCourseOfferingGuestPolicyCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.professor.ChangeCourseOfferingProfessorCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.professor.ChangeCourseOfferingProfessorCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsQuery;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsQueryHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsResponse;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseOfferingControllerTest {

    private static final Long OFFERING_ID = 100L;
    private static final Long COURSE_ID = 10L;
    private static final Long SEMESTER_ID = 1L;
    private static final Long STUDENT_ID = 5L;
    private static final String COURSE_NAME = "Database";
    private static final String COURSE_CODE = "CS305";
    private static final int CREDITS = 3;
    private static final int CAPACITY = 30;
    private static final int NEW_CAPACITY = 40;
    private static final int ENROLLED_COUNT = 25;
    private static final double GRADE = 18.5;
    private static final String PROFESSOR = "Dr. Rezaei";
    private static final String NEW_PROFESSOR = "Dr. Tehrani";
    private static final String SECTION = "01";
    private static final String CLASS_TIME = "Mon 10:00-12:00";
    private static final String EXAM_DATE = "2027-01-15";
    private static final String NEW_EXAM_DATE = "2027-01-20";
    private static final String STUDENT_NAME = "Ali Ahmadi";
    private static final String STUDENT_NUMBER = "401111";

    private static final String VALIDATION_ERROR = "Professor name cannot be blank";
    private static final String CAPACITY_ERROR = "Capacity cannot be negative";
    private static final String GRADE_ERROR = "Grade must be between 0 and 20";
    private static final String EXAM_DATE_ERROR = "Exam date cannot be in the past";
    private static final String ARCHIVED_ERROR = "Cannot offer an archived course";
    private static final String CAPACITY_LESS_THAN_ENROLLED_ERROR = "Capacity cannot be less than current enrolled count";
    private static final String DECIMAL_ERROR = "Grade can have at most 2 decimal places";
    private static final String ALREADY_GRADED_ERROR = "Can only assign grade to in-progress courses";

    private static final String BASE_URL = "/api/v1/course-offerings";
    private static final String OFFERING_URL = "/api/v1/course-offerings/{id}";
    private static final String CAPACITY_URL = "/api/v1/course-offerings/{id}/capacity";
    private static final String PROFESSOR_URL = "/api/v1/course-offerings/{id}/professor";
    private static final String GUEST_POLICY_URL = "/api/v1/course-offerings/{id}/guest-policy";
    private static final String EXAM_DATE_URL = "/api/v1/course-offerings/{id}/exam-date";
    private static final String GRADES_URL = "/api/v1/course-offerings/{id}/grades";
    private static final String STUDENTS_URL = "/api/v1/course-offerings/{id}/students";

    @Mock
    private CreateCourseOfferingCommandHandler createCourseOfferingCommandHandler;

    @Mock
    private CancelCourseOfferingCommandHandler cancelCourseOfferingCommandHandler;

    @Mock
    private ChangeCourseOfferingCapacityCommandHandler changeCourseOfferingCapacityCommandHandler;

    @Mock
    private ChangeCourseOfferingProfessorCommandHandler changeCourseOfferingProfessorCommandHandler;

    @Mock
    private ChangeCourseOfferingGuestPolicyCommandHandler changeCourseOfferingGuestPolicyCommandHandler;

    @Mock
    private RescheduleCourseOfferingExamCommandHandler rescheduleCourseOfferingExamCommandHandler;

    @Mock
    private SubmitStudentGradeCommandHandler submitStudentGradeCommandHandler;

    @Mock
    private GetCourseOfferingDetailsQueryHandler getCourseOfferingDetailsQueryHandler;

    @Mock
    private GetCourseOfferingStudentsQueryHandler getCourseOfferingStudentsQueryHandler;

    @InjectMocks
    private CourseOfferingController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created when creating course offering successfully")
    void shouldCreateCourseOfferingSuccessfully() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenReturn(OFFERING_ID);

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00",
                    "examDate": "2027-01-15"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/course-offerings/" + OFFERING_ID))
                .andExpect(jsonPath("$").value(OFFERING_ID));

        verify(createCourseOfferingCommandHandler).handle(any(CreateCourseOfferingCommand.class));
    }

    @Test
    @DisplayName("Should return 201 Created when creating course offering with optional null examDate")
    void shouldCreateOfferingSuccessfullyWhenExamDateIsNull() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenReturn(OFFERING_ID);

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00",
                    "examDate": null
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(OFFERING_ID));
    }

    @Test
    @DisplayName("Should return 404 Not Found when course does not exist on create offering")
    void shouldReturnNotFoundWhenCourseDoesNotExistOnCreateOffering() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenThrow(new CourseNotFoundException(COURSE_ID));

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when course or semester is archived on create offering")
    void shouldReturnBadRequestWhenCourseOrSemesterIsArchivedOnCreate() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenThrow(new CommandValidationException(ARCHIVED_ERROR));

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ARCHIVED_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when capacity is zero or negative on create offering")
    void shouldReturnBadRequestWhenCapacityIsZeroOrNegativeOnCreate() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenThrow(new CommandValidationException(CAPACITY_ERROR));

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 0,
                    "classTime": "Mon 10:00-12:00"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CAPACITY_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when exam date is outside semester dates on create offering")
    void shouldReturnBadRequestWhenExamDateIsOutsideSemesterOnCreate() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenThrow(new CommandValidationException("Exam date must be within semester dates"));

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "Dr. Rezaei",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00",
                    "examDate": "2030-01-01"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when create course offering validation fails")
    void shouldReturnBadRequestWhenCreateOfferingValidationFails() throws Exception {
        when(createCourseOfferingCommandHandler.handle(any(CreateCourseOfferingCommand.class)))
                .thenThrow(new CommandValidationException(VALIDATION_ERROR));

        String jsonPayload = """
                {
                    "courseId": 10,
                    "semesterId": 1,
                    "professorName": "",
                    "section": "01",
                    "guestAllowed": true,
                    "capacity": 30,
                    "classTime": "Mon 10:00-12:00",
                    "examDate": "2027-01-15"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when cancelling offering")
    void shouldCancelCourseOfferingSuccessfully() throws Exception {
        mockMvc.perform(delete(OFFERING_URL, OFFERING_ID))
                .andExpect(status().isNoContent());

        verify(cancelCourseOfferingCommandHandler).handle(new CancelCourseOfferingCommand(OFFERING_ID));
    }

    @Test
    @DisplayName("Should return 404 Not Found when cancelling non-existent offering")
    void shouldReturnNotFoundWhenCancellingNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(cancelCourseOfferingCommandHandler).handle(new CancelCourseOfferingCommand(OFFERING_ID));

        mockMvc.perform(delete(OFFERING_URL, OFFERING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when changing capacity")
    void shouldChangeCapacitySuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "capacity": 40
                }
                """;

        mockMvc.perform(put(CAPACITY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(changeCourseOfferingCapacityCommandHandler).handle(any(ChangeCourseOfferingCapacityCommand.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when changing capacity below enrolled count")
    void shouldReturnBadRequestWhenCapacityIsLowerThanEnrolledStudents() throws Exception {
        doThrow(new CommandValidationException(CAPACITY_LESS_THAN_ENROLLED_ERROR))
                .when(changeCourseOfferingCapacityCommandHandler).handle(any(ChangeCourseOfferingCapacityCommand.class));

        String jsonPayload = """
                {
                    "capacity": 4
                }
                """;

        mockMvc.perform(put(CAPACITY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CAPACITY_LESS_THAN_ENROLLED_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when changing capacity for non-existent offering")
    void shouldReturnNotFoundWhenChangingCapacityForNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(changeCourseOfferingCapacityCommandHandler).handle(any(ChangeCourseOfferingCapacityCommand.class));

        String jsonPayload = """
                {
                    "capacity": 40
                }
                """;

        mockMvc.perform(put(CAPACITY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when change capacity validation fails")
    void shouldReturnBadRequestWhenChangeCapacityValidationFails() throws Exception {
        doThrow(new CommandValidationException(CAPACITY_ERROR))
                .when(changeCourseOfferingCapacityCommandHandler).handle(any(ChangeCourseOfferingCapacityCommand.class));

        String jsonPayload = """
                {
                    "capacity": -5
                }
                """;

        mockMvc.perform(put(CAPACITY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CAPACITY_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when changing professor")
    void shouldChangeProfessorSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "professorName": "Dr. Tehrani"
                }
                """;

        mockMvc.perform(put(PROFESSOR_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(changeCourseOfferingProfessorCommandHandler).handle(any(ChangeCourseOfferingProfessorCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when changing professor for non-existent offering")
    void shouldReturnNotFoundWhenChangingProfessorForNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(changeCourseOfferingProfessorCommandHandler).handle(any(ChangeCourseOfferingProfessorCommand.class));

        String jsonPayload = """
                {
                    "professorName": "Dr. Tehrani"
                }
                """;

        mockMvc.perform(put(PROFESSOR_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when change professor validation fails")
    void shouldReturnBadRequestWhenChangeProfessorValidationFails() throws Exception {
        doThrow(new CommandValidationException(VALIDATION_ERROR))
                .when(changeCourseOfferingProfessorCommandHandler).handle(any(ChangeCourseOfferingProfessorCommand.class));

        String jsonPayload = """
                {
                    "professorName": ""
                }
                """;

        mockMvc.perform(put(PROFESSOR_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when changing guest policy")
    void shouldChangeGuestPolicySuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "guestAllowed": false
                }
                """;

        mockMvc.perform(put(GUEST_POLICY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(changeCourseOfferingGuestPolicyCommandHandler).handle(any(ChangeCourseOfferingGuestPolicyCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when changing guest policy for non-existent offering")
    void shouldReturnNotFoundWhenChangingGuestPolicyForNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(changeCourseOfferingGuestPolicyCommandHandler).handle(any(ChangeCourseOfferingGuestPolicyCommand.class));

        String jsonPayload = """
                {
                    "guestAllowed": false
                }
                """;

        mockMvc.perform(put(GUEST_POLICY_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when rescheduling exam date")
    void shouldRescheduleExamSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "examDate": "2027-01-20"
                }
                """;

        mockMvc.perform(put(EXAM_DATE_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(rescheduleCourseOfferingExamCommandHandler).handle(any(RescheduleCourseOfferingExamCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when rescheduling exam for non-existent offering")
    void shouldReturnNotFoundWhenReschedulingExamForNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(rescheduleCourseOfferingExamCommandHandler).handle(any(RescheduleCourseOfferingExamCommand.class));

        String jsonPayload = """
                {
                    "examDate": "2027-01-20"
                }
                """;

        mockMvc.perform(put(EXAM_DATE_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when reschedule exam validation fails")
    void shouldReturnBadRequestWhenRescheduleExamValidationFails() throws Exception {
        doThrow(new CommandValidationException(EXAM_DATE_ERROR))
                .when(rescheduleCourseOfferingExamCommandHandler).handle(any(RescheduleCourseOfferingExamCommand.class));

        String jsonPayload = """
                {
                    "examDate": "2020-01-01"
                }
                """;

        mockMvc.perform(put(EXAM_DATE_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(EXAM_DATE_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when submitting student grade")
    void shouldSubmitGradeSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "studentId": 5,
                    "grade": 18.5
                }
                """;

        mockMvc.perform(put(GRADES_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(submitStudentGradeCommandHandler).handle(any(SubmitStudentGradeCommand.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when submitting grade for non-existent offering")
    void shouldReturnNotFoundWhenSubmittingGradeForNonExistentOffering() throws Exception {
        doThrow(new CourseOfferingNotFoundException(OFFERING_ID))
                .when(submitStudentGradeCommandHandler).handle(any(SubmitStudentGradeCommand.class));

        String jsonPayload = """
                {
                    "studentId": 5,
                    "grade": 18.5
                }
                """;

        mockMvc.perform(put(GRADES_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when grade is invalid")
    void shouldReturnBadRequestWhenGradeValidationFails() throws Exception {
        doThrow(new CommandValidationException(GRADE_ERROR))
                .when(submitStudentGradeCommandHandler).handle(any(SubmitStudentGradeCommand.class));

        String jsonPayload = """
                {
                    "studentId": 5,
                    "grade": 25.0
                }
                """;

        mockMvc.perform(put(GRADES_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(GRADE_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when grade has more than two decimal places")
    void shouldReturnBadRequestWhenGradeHasExcessiveDecimals() throws Exception {
        doThrow(new CommandValidationException(DECIMAL_ERROR))
                .when(submitStudentGradeCommandHandler).handle(any(SubmitStudentGradeCommand.class));

        String jsonPayload = """
                {
                    "studentId": 5,
                    "grade": 17.125
                }
                """;

        mockMvc.perform(put(GRADES_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(DECIMAL_ERROR));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when submitting grade for already graded course")
    void shouldReturnUnprocessableEntityWhenCourseAlreadyGraded() throws Exception {
        doThrow(new InvalidEnrollmentStateException(ALREADY_GRADED_ERROR))
                .when(submitStudentGradeCommandHandler).handle(any(SubmitStudentGradeCommand.class));

        String jsonPayload = """
                {
                    "studentId": 5,
                    "grade": 18.0
                }
                """;

        mockMvc.perform(put(GRADES_URL, OFFERING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(ALREADY_GRADED_ERROR));
    }

    @Test
    @DisplayName("Should return 200 OK with course offering details")
    void shouldGetOfferingDetailsSuccessfully() throws Exception {
        GetCourseOfferingDetailsResponse response = new GetCourseOfferingDetailsResponse(
                OFFERING_ID, COURSE_ID, COURSE_NAME, COURSE_CODE, CREDITS,
                SEMESTER_ID, "Fall 2026", PROFESSOR, SECTION,
                true, CAPACITY, ENROLLED_COUNT, CLASS_TIME, LocalDate.of(2027, 1, 15), false
        );

        when(getCourseOfferingDetailsQueryHandler.handle(new GetCourseOfferingDetailsQuery(OFFERING_ID)))
                .thenReturn(response);

        mockMvc.perform(get(OFFERING_URL, OFFERING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offeringId").value(OFFERING_ID))
                .andExpect(jsonPath("$.courseName").value(COURSE_NAME))
                .andExpect(jsonPath("$.capacity").value(CAPACITY))
                .andExpect(jsonPath("$.enrolledCount").value(ENROLLED_COUNT));
    }

    @Test
    @DisplayName("Should return 404 Not Found when details requested for non-existent offering")
    void shouldReturnNotFoundWhenDetailsRequestedForNonExistentOffering() throws Exception {
        when(getCourseOfferingDetailsQueryHandler.handle(new GetCourseOfferingDetailsQuery(OFFERING_ID)))
                .thenThrow(new CourseOfferingNotFoundException(OFFERING_ID));

        mockMvc.perform(get(OFFERING_URL, OFFERING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with students enrolled in offering")
    void shouldGetStudentsEnrolledInOfferingSuccessfully() throws Exception {
        GetCourseOfferingStudentsResponse.CourseOfferingStudentDto studentDto =
                new GetCourseOfferingStudentsResponse.CourseOfferingStudentDto(
                        1L, STUDENT_NAME, STUDENT_NUMBER, GRADE, CourseStatus.PASSED
                );

        GetCourseOfferingStudentsResponse response = new GetCourseOfferingStudentsResponse(
                OFFERING_ID, List.of(studentDto)
        );

        when(getCourseOfferingStudentsQueryHandler.handle(new GetCourseOfferingStudentsQuery(OFFERING_ID)))
                .thenReturn(response);

        mockMvc.perform(get(STUDENTS_URL, OFFERING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseOfferingId").value(OFFERING_ID))
                .andExpect(jsonPath("$.students[0].fullName").value(STUDENT_NAME))
                .andExpect(jsonPath("$.students[0].grade").value(GRADE));
    }

    @Test
    @DisplayName("Should return 200 OK with empty list when offering has no enrolled students")
    void shouldGetEmptyStudentsListSuccessfully() throws Exception {
        GetCourseOfferingStudentsResponse response = new GetCourseOfferingStudentsResponse(
                OFFERING_ID, Collections.emptyList()
        );

        when(getCourseOfferingStudentsQueryHandler.handle(new GetCourseOfferingStudentsQuery(OFFERING_ID)))
                .thenReturn(response);

        mockMvc.perform(get(STUDENTS_URL, OFFERING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseOfferingId").value(OFFERING_ID))
                .andExpect(jsonPath("$.students").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 Not Found when students requested for non-existent offering")
    void shouldReturnNotFoundWhenStudentsRequestedForNonExistentOffering() throws Exception {
        when(getCourseOfferingStudentsQueryHandler.handle(new GetCourseOfferingStudentsQuery(OFFERING_ID)))
                .thenThrow(new CourseOfferingNotFoundException(OFFERING_ID));

        mockMvc.perform(get(STUDENTS_URL, OFFERING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
