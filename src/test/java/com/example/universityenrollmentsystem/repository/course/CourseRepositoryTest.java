package com.example.universityenrollmentsystem.repository.course;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseRepositoryTest {

    private static final String COURSE_NAME = "Advanced Programming";
    private static final String COURSE_CODE = "CS201";
    private static final String PREREQUISITE_NAME = "Basic Programming";
    private static final String PREREQUISITE_CODE = "CS101";
    private static final String SECOND_PREREQUISITE_NAME = "Mathematics 1";
    private static final String SECOND_PREREQUISITE_CODE = "MATH101";
    private static final int CREDITS = 3;
    private static final int MIN_CREDITS = 1;
    private static final int MAX_CREDITS = 4;
    private static final int UPDATED_CREDITS = 4;
    private static final String NON_EXISTING_CODE = "NOT_EXIST";

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("Should return true when course code exists in database")
    void shouldReturnTrueWhenCourseCodeExists() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);

        boolean exists = courseRepository.existsByCourseCode(COURSE_CODE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when course code does not exist in database")
    void shouldReturnFalseWhenCourseCodeDoesNotExist() {
        boolean exists = courseRepository.existsByCourseCode(NON_EXISTING_CODE);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when inserting duplicate course code")
    void shouldThrowExceptionWhenInsertingDuplicateCourseCode() {
        Course course1 = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course1);

        Course course2 = new Course("Other Name", COURSE_CODE, CREDITS);

        assertThatThrownBy(() -> courseRepository.saveAndFlush(course2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should persist and load course with prerequisites via join table")
    void shouldPersistAndLoadCourseWithPrerequisites() {
        Course prerequisite = new Course(PREREQUISITE_NAME, PREREQUISITE_CODE, CREDITS);
        courseRepository.saveAndFlush(prerequisite);

        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        course.addPrerequisite(prerequisite);
        Course savedCourse = courseRepository.saveAndFlush(course);

        Optional<Course> loadedCourse = courseRepository.findById(savedCourse.getId());

        assertThat(loadedCourse).isPresent();
        assertThat(loadedCourse.get().getPrerequisites()).hasSize(1);
        assertThat(loadedCourse.get().getPrerequisites().get(0).getCourseCode()).isEqualTo(PREREQUISITE_CODE);
    }

    @Test
    @DisplayName("Should clear prerequisites without physically deleting prerequisite courses")
    void shouldClearPrerequisitesWithoutDeletingPrerequisiteCourses() {
        Course prereq1 = new Course(PREREQUISITE_NAME, PREREQUISITE_CODE, CREDITS);
        Course prereq2 = new Course(SECOND_PREREQUISITE_NAME, SECOND_PREREQUISITE_CODE, CREDITS);
        courseRepository.saveAndFlush(prereq1);
        courseRepository.saveAndFlush(prereq2);

        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        course.addPrerequisite(prereq1);
        course.addPrerequisite(prereq2);
        Course savedCourse = courseRepository.saveAndFlush(course);

        savedCourse.clearPrerequisites();
        courseRepository.saveAndFlush(savedCourse);

        Optional<Course> reloadedCourse = courseRepository.findById(savedCourse.getId());
        Optional<Course> reloadedPrereq1 = courseRepository.findById(prereq1.getId());
        Optional<Course> reloadedPrereq2 = courseRepository.findById(prereq2.getId());

        assertThat(reloadedCourse).isPresent();
        assertThat(reloadedCourse.get().getPrerequisites()).isEmpty();
        assertThat(reloadedPrereq1).isPresent();
        assertThat(reloadedPrereq2).isPresent();
    }

    @Test
    @DisplayName("Should persist and load courses at credit boundaries (1 and 4)")
    void shouldPersistAndLoadCoursesAtCreditBoundaries() {
        Course minCreditCourse = new Course("Lab Course", "LAB101", MIN_CREDITS);
        Course maxCreditCourse = new Course("Core Project", "PRJ401", MAX_CREDITS);
        courseRepository.saveAndFlush(minCreditCourse);
        courseRepository.saveAndFlush(maxCreditCourse);

        Optional<Course> loadedMin = courseRepository.findById(minCreditCourse.getId());
        Optional<Course> loadedMax = courseRepository.findById(maxCreditCourse.getId());

        assertThat(loadedMin).isPresent();
        assertThat(loadedMin.get().getCredits()).isEqualTo(MIN_CREDITS);
        assertThat(loadedMax).isPresent();
        assertThat(loadedMax.get().getCredits()).isEqualTo(MAX_CREDITS);
    }

    @Test
    @DisplayName("Should persist soft delete state when course is marked as deleted")
    void shouldPersistSoftDeleteStateWhenCourseIsMarkedAsDeleted() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        course.markAsDeleted();
        Course savedCourse = courseRepository.saveAndFlush(course);

        Optional<Course> loadedCourse = courseRepository.findById(savedCourse.getId());

        assertThat(loadedCourse).isPresent();
        assertThat(loadedCourse.get().isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should persist updated credits in database correctly")
    void shouldPersistUpdatedCreditsCorrectly() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        Course savedCourse = courseRepository.saveAndFlush(course);

        savedCourse.updateCredits(UPDATED_CREDITS);
        courseRepository.saveAndFlush(savedCourse);

        Optional<Course> loadedCourse = courseRepository.findById(savedCourse.getId());

        assertThat(loadedCourse).isPresent();
        assertThat(loadedCourse.get().getCredits()).isEqualTo(UPDATED_CREDITS);
    }
}
