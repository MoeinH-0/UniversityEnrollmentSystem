package com.example.universityenrollmentsystem.service.courses.archive;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
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
class ArchiveCourseCommandHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final int CREDITS = 3;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ArchiveCourseCommandValidator validator;

    @InjectMocks
    private ArchiveCourseCommandHandler handler;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course("Software Engineering", "SE101", CREDITS);
    }

    @Test
    @DisplayName("Should archive course successfully and set isDeleted to true")
    void shouldArchiveCourseSuccessfully() {
        ArchiveCourseCommand command = new ArchiveCourseCommand(COURSE_ID);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        handler.handle(command);

        assertThat(course.isDeleted()).isTrue();
        verify(validator).validate(command);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        ArchiveCourseCommand command = new ArchiveCourseCommand(COURSE_ID);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(command);
        verify(courseRepository, never()).save(any());
    }
}
