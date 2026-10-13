package com.example.universityenrollmentsystem.repository.course;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseOfferingRepositoryTest {

    private static final String COURSE_NAME = "Database Systems";
    private static final String COURSE_CODE = "CS305";
    private static final int CREDITS = 3;
    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final String OTHER_SEMESTER_TITLE = "Spring 2027";
    private static final String PROFESSOR = "Dr. Rezaei";
    private static final String SECTION_1 = "01";
    private static final String SECTION_2 = "02";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Mon 10:00-12:00";
    private static final String UPDATED_CLASS_TIME = "Wed 14:00-16:00";
    private static final LocalDate EXAM_DATE_SEM1 = LocalDate.of(2027, 1, 15);
    private static final LocalDate UPDATED_EXAM_DATE = LocalDate.of(2027, 1, 18);
    private static final LocalDate EXAM_DATE_SEM2 = LocalDate.of(2027, 6, 15);
    private static final Long NON_EXISTING_ID = 99999L;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    private Course course;
    private Semester semester1;
    private Semester semester2;

    @BeforeEach
    void setUp() {
        course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);

        semester1 = new Semester(SEMESTER_TITLE, LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        semesterRepository.saveAndFlush(semester1);

        semester2 = new Semester(OTHER_SEMESTER_TITLE, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 20));
        semesterRepository.saveAndFlush(semester2);
    }

    @Test
    @DisplayName("Should return offerings filtered by semesterId")
    void shouldFindOfferingsBySemesterId() {
        CourseOffering offering1 = new CourseOffering(course, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        CourseOffering offering2 = new CourseOffering(course, semester1, PROFESSOR, SECTION_2, false, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        CourseOffering offeringOtherSemester = new CourseOffering(course, semester2, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM2);

        courseOfferingRepository.saveAndFlush(offering1);
        courseOfferingRepository.saveAndFlush(offering2);
        courseOfferingRepository.saveAndFlush(offeringOtherSemester);

        List<CourseOffering> result = courseOfferingRepository.findBySemesterId(semester1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourseOffering::getSection).containsExactlyInAnyOrder(SECTION_1, SECTION_2);
    }

    @Test
    @DisplayName("Should return empty list for existing semester with no offerings")
    void shouldReturnEmptyListForExistingSemesterWithNoOfferings() {
        List<CourseOffering> result = courseOfferingRepository.findBySemesterId(semester2.getId());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when semester id does not exist")
    void shouldReturnEmptyListWhenSemesterDoesNotExist() {
        List<CourseOffering> result = courseOfferingRepository.findBySemesterId(NON_EXISTING_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should count total offerings for a specific course across semesters")
    void shouldCountTotalOfferingsByCourseId() {
        CourseOffering offering1 = new CourseOffering(course, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        CourseOffering offering2 = new CourseOffering(course, semester2, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM2);

        courseOfferingRepository.saveAndFlush(offering1);
        courseOfferingRepository.saveAndFlush(offering2);

        int count = courseOfferingRepository.countByCourseId(course.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero when counting offerings for course with no offerings")
    void shouldReturnZeroWhenCourseHasNoOfferings() {
        int count = courseOfferingRepository.countByCourseId(NON_EXISTING_ID);

        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should fail when attempting to persist offering with unsaved transient course")
    void shouldFailWhenSavingOfferingWithUnsavedCourse() {
        Course unsavedCourse = new Course("Transient Course", "TRN999", CREDITS);
        CourseOffering offering = new CourseOffering(unsavedCourse, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);

        assertThatThrownBy(() -> courseOfferingRepository.saveAndFlush(offering))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should persist and update offering details correctly")
    void shouldPersistAndModifyOffering() {
        CourseOffering offering = new CourseOffering(course, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        CourseOffering saved = courseOfferingRepository.saveAndFlush(offering);

        saved.updateCapacity(50);
        saved.changeProfessor("Dr. Tehrani");
        saved.updateGuestPolicy(false);
        courseOfferingRepository.saveAndFlush(saved);

        Optional<CourseOffering> reloaded = courseOfferingRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCapacity()).isEqualTo(50);
        assertThat(reloaded.get().getProfessorName()).isEqualTo("Dr. Tehrani");
        assertThat(reloaded.get().isGuestAllowed()).isFalse();
    }

    @Test
    @DisplayName("Should persist rescheduled exam date and class time")
    void shouldPersistRescheduledExamDateAndClassTime() {
        CourseOffering offering = new CourseOffering(course, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        CourseOffering saved = courseOfferingRepository.saveAndFlush(offering);

        saved.updateClassTime(UPDATED_CLASS_TIME);
        saved.updateExamDate(UPDATED_EXAM_DATE);
        courseOfferingRepository.saveAndFlush(saved);

        Optional<CourseOffering> reloaded = courseOfferingRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getClassTime()).isEqualTo(UPDATED_CLASS_TIME);
        assertThat(reloaded.get().getExamDate()).isEqualTo(UPDATED_EXAM_DATE);
    }

    @Test
    @DisplayName("Should persist soft delete state when offering is marked as deleted")
    void shouldPersistSoftDeleteStateWhenOfferingIsMarkedAsDeleted() {
        CourseOffering offering = new CourseOffering(course, semester1, PROFESSOR, SECTION_1, true, CAPACITY, CLASS_TIME, EXAM_DATE_SEM1);
        offering.markAsDeleted();
        CourseOffering saved = courseOfferingRepository.saveAndFlush(offering);

        Optional<CourseOffering> reloaded = courseOfferingRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().isDeleted()).isTrue();
    }
}
