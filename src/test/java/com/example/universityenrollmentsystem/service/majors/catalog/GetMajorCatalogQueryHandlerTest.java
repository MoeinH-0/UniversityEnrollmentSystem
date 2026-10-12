package com.example.universityenrollmentsystem.service.majors.catalog;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMajorCatalogQueryHandlerTest {

    private static final Long MAJOR_ID = 1L;
    private static final String MAJOR_NAME = "Computer Engineering";
    private static final Long COURSE1_ID = 10L;
    private static final String COURSE1_NAME = "Data Structures";
    private static final String COURSE1_CODE = "CS201";
    private static final int COURSE1_CREDITS = 3;
    private static final Long COURSE2_ID = 20L;
    private static final String COURSE2_NAME = "Archived Course";
    private static final String COURSE2_CODE = "CS299";
    private static final int COURSE2_CREDITS = 2;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private GetMajorCatalogQueryValidator validator;

    @InjectMocks
    private GetMajorCatalogQueryHandler handler;

    private Major major;
    private Course activeCourse;
    private Course deletedCourse;

    @BeforeEach
    void setUp() {
        major = new Major(MAJOR_NAME);
        ReflectionTestUtils.setField(major, "id", MAJOR_ID);

        activeCourse = new Course(COURSE1_NAME, COURSE1_CODE, COURSE1_CREDITS);
        ReflectionTestUtils.setField(activeCourse, "id", COURSE1_ID);

        deletedCourse = new Course(COURSE2_NAME, COURSE2_CODE, COURSE2_CREDITS);
        ReflectionTestUtils.setField(deletedCourse, "id", COURSE2_ID);
        deletedCourse.markAsDeleted();

        major.addCourse(activeCourse);
        major.addCourse(deletedCourse);
    }

    @Test
    @DisplayName("Should return major catalog excluding deleted courses")
    void shouldReturnMajorCatalogExcludingDeletedCourses() {
        GetMajorCatalogQuery query = new GetMajorCatalogQuery(MAJOR_ID);
        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));

        GetMajorCatalogResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.majorId()).isEqualTo(MAJOR_ID);
        assertThat(response.majorName()).isEqualTo(MAJOR_NAME);
        assertThat(response.courses()).hasSize(1);

        GetMajorCatalogResponse.MajorCatalogCourseDto dto = response.courses().get(0);
        assertThat(dto.courseId()).isEqualTo(COURSE1_ID);
        assertThat(dto.name()).isEqualTo(COURSE1_NAME);
        assertThat(dto.courseCode()).isEqualTo(COURSE1_CODE);
        assertThat(dto.credits()).isEqualTo(COURSE1_CREDITS);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major is not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        GetMajorCatalogQuery query = new GetMajorCatalogQuery(MAJOR_ID);
        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(MajorNotFoundException.class);

        verify(validator).validate(query);
    }
}
