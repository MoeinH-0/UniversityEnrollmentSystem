package com.example.universityenrollmentsystem.repository.academic;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MajorRepositoryTest {

    private static final String MAJOR_NAME = "Computer Engineering";
    private static final String LOWER_CASE_NAME = "computer engineering";
    private static final String RENAMED_MAJOR = "Software Engineering";
    private static final String NON_EXISTING_NAME = "Non Existing Major";
    private static final String COURSE_NAME = "Data Structures";
    private static final String COURSE_CODE = "CS202";
    private static final int CREDITS = 3;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("Should return true when major name exists in database")
    void shouldReturnTrueWhenMajorNameExists() {
        Major major = new Major(MAJOR_NAME);
        majorRepository.saveAndFlush(major);

        boolean exists = majorRepository.existsByName(MAJOR_NAME);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when major name does not exist in database")
    void shouldReturnFalseWhenMajorNameDoesNotExist() {
        boolean exists = majorRepository.existsByName(NON_EXISTING_NAME);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should be case sensitive when checking existsByName")
    void shouldBeCaseSensitiveWhenCheckingExistsByName() {
        Major major = new Major(MAJOR_NAME);
        majorRepository.saveAndFlush(major);

        boolean exists = majorRepository.existsByName(LOWER_CASE_NAME);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true for existsByName even when major is soft deleted")
    void shouldReturnTrueForExistsByNameEvenWhenMajorIsSoftDeleted() {
        Major major = new Major(MAJOR_NAME);
        major.markAsDeleted();
        majorRepository.saveAndFlush(major);

        boolean exists = majorRepository.existsByName(MAJOR_NAME);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should persist and load major with its associated courses")
    void shouldPersistAndLoadMajorWithCourses() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);

        Major major = new Major(MAJOR_NAME);
        major.addCourse(course);
        Major savedMajor = majorRepository.saveAndFlush(major);

        Optional<Major> foundMajor = majorRepository.findById(savedMajor.getId());

        assertThat(foundMajor).isPresent();
        assertThat(foundMajor.get().getCourses()).hasSize(1);
        assertThat(foundMajor.get().getCourses().get(0).getCourseCode()).isEqualTo(COURSE_CODE);
    }

    @Test
    @DisplayName("Should not physically delete course when major is deleted")
    void shouldNotPhysicallyDeleteCourseWhenMajorIsDeleted() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);

        Major major = new Major(MAJOR_NAME);
        major.addCourse(course);
        Major savedMajor = majorRepository.saveAndFlush(major);

        majorRepository.delete(savedMajor);
        majorRepository.flush();

        Optional<Course> courseAfterMajorDelete = courseRepository.findById(course.getId());

        assertThat(courseAfterMajorDelete).isPresent();
    }

    @Test
    @DisplayName("Should persist removal of course from major")
    void shouldRemoveCourseFromMajorAndPersist() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);

        Major major = new Major(MAJOR_NAME);
        major.addCourse(course);
        Major savedMajor = majorRepository.saveAndFlush(major);

        savedMajor.removeCourse(course);
        majorRepository.saveAndFlush(savedMajor);

        Optional<Major> foundMajor = majorRepository.findById(savedMajor.getId());

        assertThat(foundMajor).isPresent();
        assertThat(foundMajor.get().getCourses()).isEmpty();
    }

    @Test
    @DisplayName("Should persist major rename correctly")
    void shouldPersistMajorRenameCorrectly() {
        Major major = new Major(MAJOR_NAME);
        Major savedMajor = majorRepository.saveAndFlush(major);

        savedMajor.updateName(RENAMED_MAJOR);
        majorRepository.saveAndFlush(savedMajor);

        Optional<Major> foundMajor = majorRepository.findById(savedMajor.getId());

        assertThat(foundMajor).isPresent();
        assertThat(foundMajor.get().getName()).isEqualTo(RENAMED_MAJOR);
    }

    @Test
    @DisplayName("Should persist soft delete state when major is marked as deleted")
    void shouldPersistSoftDeleteState() {
        Major major = new Major(MAJOR_NAME);
        major.markAsDeleted();
        Major savedMajor = majorRepository.saveAndFlush(major);

        Optional<Major> foundMajor = majorRepository.findById(savedMajor.getId());

        assertThat(foundMajor).isPresent();
        assertThat(foundMajor.get().isDeleted()).isTrue();
    }
}
