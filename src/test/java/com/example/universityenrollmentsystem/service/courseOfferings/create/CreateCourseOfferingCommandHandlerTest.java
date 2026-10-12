package com.example.universityenrollmentsystem.service.courseOfferings.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCourseOfferingCommandHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final Long SEMESTER_ID = 2L;
    private static final String PROFESSOR = "Dr. Hosseini";
    private static final String SECTION = "01";
    private static final boolean GUEST_ALLOWED = true;
    private static final int CAPACITY = 30;
    private static final int COURSE_CREDITS = 3;
    private static final String CLASS_TIME = "Mon 10:00";
    private static final LocalDate EXAM_DATE = LocalDate.of(2027, 1, 15);

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private CreateCourseOfferingCommandValidator validator;

    @InjectMocks
    private CreateCourseOfferingCommandHandler handler;

    private CreateCourseOfferingCommand command;
    private Course course;
    private Semester semester;

    @BeforeEach
    void setUp() {
        command = new CreateCourseOfferingCommand(
                COURSE_ID, SEMESTER_ID, PROFESSOR, SECTION,
                GUEST_ALLOWED, CAPACITY, CLASS_TIME, EXAM_DATE);

        course = new Course("Operating Systems", "CS401", COURSE_CREDITS);
        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
    }

    @Test
    @DisplayName("Should create course offering successfully when all arguments are valid")
    void shouldCreateCourseOfferingSuccessfully() {
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));

        handler.handle(command);

        verify(validator).validate(command);
        verify(courseOfferingRepository).save(any(CourseOffering.class));
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommandValidationException when course is archived (deleted)")
    void shouldThrowExceptionWhenCourseIsArchived() {
        course.markAsDeleted();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommandValidationException when semester is archived (deleted)")
    void shouldThrowExceptionWhenSemesterIsArchived() {
        semester.markAsDeleted();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }
}
