package com.example.universityenrollmentsystem.service.courses.prerequisites;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidCourseStateException;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCoursePrerequisitesCommandHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final Long PREREQ_ID = 2L;
    private static final int CREDITS = 3;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UpdateCoursePrerequisitesCommandValidator validator;

    @InjectMocks
    private UpdateCoursePrerequisitesCommandHandler handler;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course("Data Structures", "CS201", CREDITS);
    }

    @Test
    @DisplayName("Should clear prerequisites when prerequisiteIds is null")
    void shouldClearPrerequisitesWhenPrerequisiteIdsNull() {
        Course existingPrereq = new Course("Basic Programming", "CS101", CREDITS);
        course.addPrerequisite(existingPrereq);

        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(COURSE_ID, null);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        handler.handle(command);

        assertThat(course.getPrerequisites()).isEmpty();
        verify(validator).validate(command);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should clear prerequisites when prerequisiteIds is empty")
    void shouldClearPrerequisitesWhenPrerequisiteIdsEmpty() {
        Course existingPrereq = new Course("Basic Programming", "CS101", CREDITS);
        course.addPrerequisite(existingPrereq);

        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(COURSE_ID, List.of());

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        handler.handle(command);

        assertThat(course.getPrerequisites()).isEmpty();
        verify(validator).validate(command);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should update prerequisites successfully with valid list")
    void shouldUpdatePrerequisitesSuccessfully() {
        Course prerequisite = new Course("Basic Programming", "CS101", CREDITS);
        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(COURSE_ID, List.of(PREREQ_ID));

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseRepository.findAllById(List.of(PREREQ_ID))).thenReturn(List.of(prerequisite));

        handler.handle(command);

        assertThat(course.getPrerequisites()).containsExactly(prerequisite);
        verify(validator).validate(command);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(COURSE_ID, List.of(PREREQ_ID));

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(command);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidCourseStateException when course is set as prerequisite of itself")
    void shouldThrowExceptionWhenCourseIsPrerequisiteOfItself() {
        UpdateCoursePrerequisitesCommand command = new UpdateCoursePrerequisitesCommand(COURSE_ID, List.of(COURSE_ID));

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseRepository.findAllById(List.of(COURSE_ID))).thenReturn(List.of(course));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidCourseStateException.class);

        verify(courseRepository, never()).save(any());
    }
}
