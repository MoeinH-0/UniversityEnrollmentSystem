package com.example.universityenrollmentsystem.service.courses.details;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCourseDetailsQueryHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final Long PREREQ_ID = 2L;
    private static final String COURSE_NAME = "Advanced Programming";
    private static final String COURSE_CODE = "AP101";
    private static final int CREDITS = 4;
    private static final String PREREQ_NAME = "Basics of Programming";
    private static final String PREREQ_CODE = "BP100";
    private static final int PREREQ_CREDITS = 3;
    private static final String MAJOR_NAME = "Computer Engineering";

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private GetCourseDetailsQueryValidator validator;

    @InjectMocks
    private GetCourseDetailsQueryHandler handler;

    private Course course;
    private Course prerequisite;
    private Major major;

    @BeforeEach
    void setUp() {
        course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        ReflectionTestUtils.setField(course, "id", COURSE_ID);

        prerequisite = new Course(PREREQ_NAME, PREREQ_CODE, PREREQ_CREDITS);
        ReflectionTestUtils.setField(prerequisite, "id", PREREQ_ID);
        course.addPrerequisite(prerequisite);

        major = new Major(MAJOR_NAME);
        major.addCourse(course);

        attachMajorToCourse(course, major);
    }

    @SuppressWarnings("unchecked")
    private void attachMajorToCourse(Course c, Major m) {
        List<Major> list = (List<Major>) ReflectionTestUtils.getField(c, "majors");
        if (list != null)
            list.add(m);
    }

    @Test
    @DisplayName("Should return course details successfully with prerequisites and majors")
    void shouldReturnCourseDetailsSuccessfully() {
        GetCourseDetailsQuery query = new GetCourseDetailsQuery(COURSE_ID);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        GetCourseDetailsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.name()).isEqualTo(COURSE_NAME);
        assertThat(response.courseCode()).isEqualTo(COURSE_CODE);
        assertThat(response.credits()).isEqualTo(CREDITS);
        assertThat(response.isDeleted()).isFalse();

        assertThat(response.prerequisites()).hasSize(1);
        GetCourseDetailsResponse.PrerequisiteDto prereqDto = response.prerequisites().get(0);
        assertThat(prereqDto.id()).isEqualTo(PREREQ_ID);
        assertThat(prereqDto.name()).isEqualTo(PREREQ_NAME);
        assertThat(prereqDto.courseCode()).isEqualTo(PREREQ_CODE);
        assertThat(prereqDto.credits()).isEqualTo(PREREQ_CREDITS);

        assertThat(response.majors()).containsExactly(MAJOR_NAME);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course does not exist")
    void shouldThrowExceptionWhenCourseNotFound() {
        GetCourseDetailsQuery query = new GetCourseDetailsQuery(COURSE_ID);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(query);
    }
}
