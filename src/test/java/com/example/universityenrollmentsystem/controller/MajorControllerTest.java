package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.controller.advice.GlobalExceptionHandler;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidMajorStateException;
import com.example.universityenrollmentsystem.service.exceptions.conflict.MajorAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import com.example.universityenrollmentsystem.service.majors.addcourse.AddCourseToMajorCommand;
import com.example.universityenrollmentsystem.service.majors.addcourse.AddCourseToMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.archive.ArchiveMajorCommand;
import com.example.universityenrollmentsystem.service.majors.archive.ArchiveMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogQuery;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogQueryHandler;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogResponse;
import com.example.universityenrollmentsystem.service.majors.create.CreateMajorCommand;
import com.example.universityenrollmentsystem.service.majors.create.CreateMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.rename.RenameMajorCommand;
import com.example.universityenrollmentsystem.service.majors.rename.RenameMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorQuery;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorQueryHandler;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorResponse;
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
class MajorControllerTest {

    private static final Long MAJOR_ID = 1L;
    private static final Long COURSE_ID = 10L;
    private static final String MAJOR_NAME = "Computer Engineering";
    private static final String NEW_MAJOR_NAME = "Software Engineering";
    private static final String VALIDATION_ERROR = "Major name cannot be blank";
    private static final String ARCHIVED_ERROR = "Cannot add to an archived major or course";
    private static final String DUPLICATE_COURSE_IN_MAJOR_ERROR = "Course is already added to this major";
    private static final String COURSE_NAME = "Algorithms";
    private static final String COURSE_CODE = "CS302";
    private static final int CREDITS = 3;
    private static final Long STUDENT_ID = 100L;
    private static final String STUDENT_NAME = "Sara Ahmadi";
    private static final String STUDENT_NUMBER = "401234";

    private static final String BASE_URL = "/api/v1/majors";
    private static final String MAJOR_URL = "/api/v1/majors/{id}";
    private static final String RENAME_URL = "/api/v1/majors/{id}/name";
    private static final String ADD_COURSE_URL = "/api/v1/majors/{id}/courses/{courseId}";
    private static final String CATALOG_URL = "/api/v1/majors/{id}/catalog";
    private static final String STUDENTS_URL = "/api/v1/majors/{id}/students";

    @Mock
    private CreateMajorCommandHandler createMajorCommandHandler;

    @Mock
    private ArchiveMajorCommandHandler archiveMajorCommandHandler;

    @Mock
    private RenameMajorCommandHandler renameMajorCommandHandler;

    @Mock
    private AddCourseToMajorCommandHandler addCourseToMajorCommandHandler;

    @Mock
    private GetMajorCatalogQueryHandler getMajorCatalogQueryHandler;

    @Mock
    private GetStudentsByMajorQueryHandler getStudentsByMajorQueryHandler;

    @InjectMocks
    private MajorController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with Location header when creating major")
    void shouldCreateMajorSuccessfully() throws Exception {
        when(createMajorCommandHandler.handle(any(CreateMajorCommand.class))).thenReturn(MAJOR_ID);

        String jsonPayload = """
                {
                    "name": "Computer Engineering"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/majors/" + MAJOR_ID))
                .andExpect(jsonPath("$").value(MAJOR_ID));

        verify(createMajorCommandHandler).handle(any(CreateMajorCommand.class));
    }

    @Test
    @DisplayName("Should return 409 Conflict when major name already exists")
    void shouldReturnConflictWhenMajorAlreadyExists() throws Exception {
        when(createMajorCommandHandler.handle(any(CreateMajorCommand.class)))
                .thenThrow(new MajorAlreadyExistsException(MAJOR_NAME));

        String jsonPayload = """
                {
                    "name": "Computer Engineering"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when create major validation fails")
    void shouldReturnBadRequestWhenCreateMajorValidationFails() throws Exception {
        when(createMajorCommandHandler.handle(any(CreateMajorCommand.class)))
                .thenThrow(new CommandValidationException(VALIDATION_ERROR));

        String jsonPayload = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when archiving major")
    void shouldArchiveMajorSuccessfully() throws Exception {
        mockMvc.perform(delete(MAJOR_URL, MAJOR_ID))
                .andExpect(status().isNoContent());

        verify(archiveMajorCommandHandler).handle(new ArchiveMajorCommand(MAJOR_ID));
    }

    @Test
    @DisplayName("Should return 404 Not Found when archiving non-existent major")
    void shouldReturnNotFoundWhenArchivingNonExistentMajor() throws Exception {
        doThrow(new MajorNotFoundException(MAJOR_ID))
                .when(archiveMajorCommandHandler).handle(new ArchiveMajorCommand(MAJOR_ID));

        mockMvc.perform(delete(MAJOR_URL, MAJOR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 204 No Content when renaming major")
    void shouldRenameMajorSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "name": "Software Engineering"
                }
                """;

        mockMvc.perform(put(RENAME_URL, MAJOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());

        verify(renameMajorCommandHandler).handle(new RenameMajorCommand(MAJOR_ID, NEW_MAJOR_NAME));
    }

    @Test
    @DisplayName("Should return 409 Conflict when renaming major to existing name")
    void shouldReturnConflictWhenRenamingMajorToExistingName() throws Exception {
        doThrow(new MajorAlreadyExistsException(NEW_MAJOR_NAME))
                .when(renameMajorCommandHandler).handle(new RenameMajorCommand(MAJOR_ID, NEW_MAJOR_NAME));

        String jsonPayload = """
                {
                    "name": "Software Engineering"
                }
                """;

        mockMvc.perform(put(RENAME_URL, MAJOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when renaming non-existent major")
    void shouldReturnNotFoundWhenRenamingNonExistentMajor() throws Exception {
        doThrow(new MajorNotFoundException(MAJOR_ID))
                .when(renameMajorCommandHandler).handle(new RenameMajorCommand(MAJOR_ID, NEW_MAJOR_NAME));

        String jsonPayload = """
                {
                    "name": "Software Engineering"
                }
                """;

        mockMvc.perform(put(RENAME_URL, MAJOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when rename major validation fails")
    void shouldReturnBadRequestWhenRenameMajorValidationFails() throws Exception {
        doThrow(new CommandValidationException(VALIDATION_ERROR))
                .when(renameMajorCommandHandler).handle(new RenameMajorCommand(MAJOR_ID, ""));

        String jsonPayload = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(put(RENAME_URL, MAJOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR));
    }

    @Test
    @DisplayName("Should return 204 No Content when adding course to major")
    void shouldAddCourseToMajorSuccessfully() throws Exception {
        mockMvc.perform(post(ADD_COURSE_URL, MAJOR_ID, COURSE_ID))
                .andExpect(status().isNoContent());

        verify(addCourseToMajorCommandHandler).handle(new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID));
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Content when adding duplicate course to major")
    void shouldReturnUnprocessableEntityWhenAddingDuplicateCourseToMajor() throws Exception {
        doThrow(new InvalidMajorStateException(DUPLICATE_COURSE_IN_MAJOR_ERROR))
                .when(addCourseToMajorCommandHandler).handle(new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID));

        mockMvc.perform(post(ADD_COURSE_URL, MAJOR_ID, COURSE_ID))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(DUPLICATE_COURSE_IN_MAJOR_ERROR));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when adding course to archived major or course")
    void shouldReturnBadRequestWhenAddingCourseToArchivedMajorOrCourse() throws Exception {
        doThrow(new CommandValidationException(ARCHIVED_ERROR))
                .when(addCourseToMajorCommandHandler).handle(new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID));

        mockMvc.perform(post(ADD_COURSE_URL, MAJOR_ID, COURSE_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ARCHIVED_ERROR));
    }

    @Test
    @DisplayName("Should return 404 Not Found when course does not exist on add course to major")
    void shouldReturnNotFoundWhenCourseDoesNotExistOnAddCourseToMajor() throws Exception {
        doThrow(new CourseNotFoundException(COURSE_ID))
                .when(addCourseToMajorCommandHandler).handle(new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID));

        mockMvc.perform(post(ADD_COURSE_URL, MAJOR_ID, COURSE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when adding course to non-existent major")
    void shouldReturnNotFoundWhenAddingCourseToNonExistentMajor() throws Exception {
        doThrow(new MajorNotFoundException(MAJOR_ID))
                .when(addCourseToMajorCommandHandler).handle(new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID));

        mockMvc.perform(post(ADD_COURSE_URL, MAJOR_ID, COURSE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with major catalog")
    void shouldGetMajorCatalogSuccessfully() throws Exception {
        GetMajorCatalogResponse.MajorCatalogCourseDto courseDto =
                new GetMajorCatalogResponse.MajorCatalogCourseDto(COURSE_ID, COURSE_NAME, COURSE_CODE, CREDITS);

        GetMajorCatalogResponse response = new GetMajorCatalogResponse(MAJOR_ID, MAJOR_NAME, List.of(courseDto));

        when(getMajorCatalogQueryHandler.handle(new GetMajorCatalogQuery(MAJOR_ID))).thenReturn(response);

        mockMvc.perform(get(CATALOG_URL, MAJOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.majorId").value(MAJOR_ID))
                .andExpect(jsonPath("$.majorName").value(MAJOR_NAME))
                .andExpect(jsonPath("$.courses[0].name").value(COURSE_NAME));
    }

    @Test
    @DisplayName("Should return 200 OK with empty catalog when major has no active courses")
    void shouldGetEmptyMajorCatalogSuccessfully() throws Exception {
        GetMajorCatalogResponse response = new GetMajorCatalogResponse(MAJOR_ID, MAJOR_NAME, Collections.emptyList());

        when(getMajorCatalogQueryHandler.handle(new GetMajorCatalogQuery(MAJOR_ID))).thenReturn(response);

        mockMvc.perform(get(CATALOG_URL, MAJOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.majorId").value(MAJOR_ID))
                .andExpect(jsonPath("$.courses").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 Not Found when major does not exist on get catalog")
    void shouldReturnNotFoundWhenMajorDoesNotExistOnCatalog() throws Exception {
        when(getMajorCatalogQueryHandler.handle(new GetMajorCatalogQuery(MAJOR_ID)))
                .thenThrow(new MajorNotFoundException(MAJOR_ID));

        mockMvc.perform(get(CATALOG_URL, MAJOR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 200 OK with students belonging to major")
    void shouldGetStudentsByMajorSuccessfully() throws Exception {
        GetStudentsByMajorResponse.MajorStudentSummaryDto studentDto =
                new GetStudentsByMajorResponse.MajorStudentSummaryDto(
                        STUDENT_ID, STUDENT_NAME, STUDENT_NUMBER, EnrollmentPlanType.REGULAR, StudentStatus.ACTIVE
                );

        GetStudentsByMajorResponse response = new GetStudentsByMajorResponse(
                MAJOR_ID, MAJOR_NAME, List.of(studentDto)
        );

        when(getStudentsByMajorQueryHandler.handle(new GetStudentsByMajorQuery(MAJOR_ID))).thenReturn(response);

        mockMvc.perform(get(STUDENTS_URL, MAJOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.majorId").value(MAJOR_ID))
                .andExpect(jsonPath("$.students[0].fullName").value(STUDENT_NAME));
    }

    @Test
    @DisplayName("Should return 200 OK with empty list when major has no students")
    void shouldGetEmptyStudentsByMajorSuccessfully() throws Exception {
        GetStudentsByMajorResponse response = new GetStudentsByMajorResponse(
                MAJOR_ID, MAJOR_NAME, Collections.emptyList()
        );

        when(getStudentsByMajorQueryHandler.handle(new GetStudentsByMajorQuery(MAJOR_ID))).thenReturn(response);

        mockMvc.perform(get(STUDENTS_URL, MAJOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.majorId").value(MAJOR_ID))
                .andExpect(jsonPath("$.students").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 Not Found when major does not exist on get students")
    void shouldReturnNotFoundWhenMajorDoesNotExistOnStudents() throws Exception {
        when(getStudentsByMajorQueryHandler.handle(new GetStudentsByMajorQuery(MAJOR_ID)))
                .thenThrow(new MajorNotFoundException(MAJOR_ID));

        mockMvc.perform(get(STUDENTS_URL, MAJOR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
