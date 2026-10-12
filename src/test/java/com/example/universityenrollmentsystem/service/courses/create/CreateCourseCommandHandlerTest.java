package com.example.universityenrollmentsystem.service.courses.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCourseCommandHandlerTest {

    private static final String COURSE_NAME = "Advanced Programming";
    private static final String COURSE_CODE = "CS202";
    private static final int CREDITS = 3;
    private static final Long MAJOR_ID = 1L;
    private static final Long PREREQ_ID = 99L;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private CreateCourseCommandValidator validator;

    @InjectMocks
    private CreateCourseCommandHandler handler;

    private Major major;

    @BeforeEach
    void setUp() {
        major = new Major("Computer Engineering");
    }

    @Test
    @DisplayName("Should create course successfully with null prerequisites")
    void shouldCreateCourseSuccessfullyWithNullPrerequisites() {
        CreateCourseCommand command = new CreateCourseCommand(
                COURSE_NAME, COURSE_CODE, CREDITS, List.of(MAJOR_ID), null);

        when(courseRepository.existsByCourseCode(COURSE_CODE)).thenReturn(false);
        when(majorRepository.findAllById(List.of(MAJOR_ID))).thenReturn(List.of(major));

        handler.handle(command);

        verify(validator).validate(command);
        verify(courseRepository).save(any(Course.class));
        verify(majorRepository).save(major);
        assertThat(major.getCourses()).hasSize(1);
    }

    @Test
    @DisplayName("Should create course successfully with empty prerequisites list")
    void shouldCreateCourseSuccessfullyWithEmptyPrerequisites() {
        CreateCourseCommand command = new CreateCourseCommand(
                COURSE_NAME, COURSE_CODE, CREDITS, List.of(MAJOR_ID), List.of());

        when(courseRepository.existsByCourseCode(COURSE_CODE)).thenReturn(false);
        when(majorRepository.findAllById(List.of(MAJOR_ID))).thenReturn(List.of(major));

        handler.handle(command);

        verify(validator).validate(command);
        verify(courseRepository).save(any(Course.class));
        verify(majorRepository).save(major);
    }

    @Test
    @DisplayName("Should create course successfully with valid prerequisites")
    void shouldCreateCourseSuccessfullyWithValidPrerequisites() {
        Course prerequisite = new Course("Basic Programming", "CS101", 3);
        CreateCourseCommand command = new CreateCourseCommand(
                COURSE_NAME, COURSE_CODE, CREDITS, List.of(MAJOR_ID), List.of(PREREQ_ID));

        when(courseRepository.existsByCourseCode(COURSE_CODE)).thenReturn(false);
        when(majorRepository.findAllById(List.of(MAJOR_ID))).thenReturn(List.of(major));
        when(courseRepository.findAllById(List.of(PREREQ_ID))).thenReturn(List.of(prerequisite));

        handler.handle(command);

        verify(validator).validate(command);
        verify(courseRepository).save(any(Course.class));
        verify(majorRepository).save(major);
    }

    @Test
    @DisplayName("Should throw CourseAlreadyExistsException when course code is duplicated")
    void shouldThrowExceptionWhenCourseCodeDuplicated() {
        CreateCourseCommand command = new CreateCourseCommand(
                COURSE_NAME, COURSE_CODE, CREDITS, List.of(MAJOR_ID), null);

        when(courseRepository.existsByCourseCode(COURSE_CODE)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseAlreadyExistsException.class);

        verify(validator).validate(command);
        verify(courseRepository, never()).save(any());
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommandValidationException when one or more majors are not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        CreateCourseCommand command = new CreateCourseCommand(
                COURSE_NAME, COURSE_CODE, CREDITS, List.of(MAJOR_ID, 2L), null);

        when(courseRepository.existsByCourseCode(COURSE_CODE)).thenReturn(false);
        when(majorRepository.findAllById(List.of(MAJOR_ID, 2L))).thenReturn(List.of(major));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(courseRepository, never()).save(any());
        verify(majorRepository, never()).save(any());
    }
}
