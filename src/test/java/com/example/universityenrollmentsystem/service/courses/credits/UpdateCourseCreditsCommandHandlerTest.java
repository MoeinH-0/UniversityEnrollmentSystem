package com.example.universityenrollmentsystem.service.courses.credits;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCourseCreditsCommandHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final int INITIAL_CREDITS = 3;
    private static final int NEW_VALID_CREDITS = 4;
    private static final int INVALID_CREDITS = 0;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UpdateCourseCreditsCommandValidator validator;

    @InjectMocks
    private UpdateCourseCreditsCommandHandler handler;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course("Software Engineering", "SE101", INITIAL_CREDITS);
    }

    @Test
    @DisplayName("Should update course credits successfully")
    void shouldUpdateCreditsSuccessfully() {
        UpdateCourseCreditsCommand command = new UpdateCourseCreditsCommand(COURSE_ID, NEW_VALID_CREDITS);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        handler.handle(command);

        assertThat(course.getCredits()).isEqualTo(NEW_VALID_CREDITS);
        verify(validator).validate(command);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        UpdateCourseCreditsCommand command = new UpdateCourseCreditsCommand(COURSE_ID, NEW_VALID_CREDITS);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(command);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when credits are outside domain limits")
    void shouldThrowExceptionWhenCreditsInvalidInDomain() {
        UpdateCourseCreditsCommand command = new UpdateCourseCreditsCommand(COURSE_ID, INVALID_CREDITS);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(DomainValidationException.class);

        verify(courseRepository, never()).save(any());
    }
}
