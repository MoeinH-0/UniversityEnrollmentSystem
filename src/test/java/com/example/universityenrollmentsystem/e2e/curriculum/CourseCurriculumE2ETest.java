package com.example.universityenrollmentsystem.e2e.curriculum;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseCurriculumE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String MAJOR_NAME_URL = "/api/v1/majors/%d/name";
    private static final String MAJOR_COURSE_URL = "/api/v1/majors/%d/courses/%d";
    private static final String MAJOR_CATALOG_URL = "/api/v1/majors/%d/catalog";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String COURSE_ARCHIVE_URL = "/api/v1/courses/%d";
    private static final String COURSE_PREREQUISITES_URL = "/api/v1/courses/%d/prerequisites";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String STUDENTS_URL = "/api/v1/students";

    private static final String COMPUTER_MAJOR = "Computer Engineering";
    private static final String INDUSTRIAL_MAJOR = "Industrial Engineering";
    private static final String ELECTRICAL_MAJOR = "Electrical Engineering";
    private static final String AI_MAJOR = "Artificial Intelligence";

    private static final String STUDENT_NAME_1 = "Ali Rezaei";
    private static final String STUDENT_NAME_2 = "Sara Mohammadi";
    private static final String STUDENT_NUMBER_1 = "401999111";
    private static final String STUDENT_NUMBER_2 = "401999222";

    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final String START_DATE = "2026-09-20";
    private static final String END_DATE = "2027-01-20";
    private static final String PROFESSOR = "Dr. Rad";
    private static final String SECTION = "01";
    private static final int CAPACITY = 25;
    private static final String CLASS_TIME = "Sun 08:00";
    private static final String EXAM_DATE = "2027-01-15";

    @Test
    @DisplayName("Should expand curriculum mid-cycle allowing regular students to enroll in newly added course")
    void shouldExpandCurriculumMidCycleAndUnlockCourseForStudents() {
        Long compMajorId = createMajor(COMPUTER_MAJOR);
        Long indMajorId = createMajor(INDUSTRIAL_MAJOR);

        Long opResearchCourseId = createCourse("Operations Research", "IE301", 3, List.of(indMajorId));

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        Long offeringId = createOffering(opResearchCourseId, semesterId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, compMajorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offeringId), HttpStatus.UNPROCESSABLE_CONTENT);

        ResponseEntity<GetMajorCatalogResponse> catalogBefore = restTemplate.getForEntity(
                String.format(MAJOR_CATALOG_URL, compMajorId),
                GetMajorCatalogResponse.class
        );
        assertThat(catalogBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(catalogBefore.getBody()).isNotNull();
        assertThat(catalogBefore.getBody().courses()).noneMatch(c -> c.courseId().equals(opResearchCourseId));

        ResponseEntity<Void> addCourseResponse = restTemplate.postForEntity(
                String.format(MAJOR_COURSE_URL, compMajorId, opResearchCourseId),
                null,
                Void.class
        );
        assertThat(addCourseResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetMajorCatalogResponse> catalogAfter = restTemplate.getForEntity(
                String.format(MAJOR_CATALOG_URL, compMajorId),
                GetMajorCatalogResponse.class
        );
        assertThat(catalogAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(catalogAfter.getBody()).isNotNull();
        assertThat(catalogAfter.getBody().courses()).anyMatch(c -> c.courseId().equals(opResearchCourseId));

        enroll(studentId, List.of(offeringId), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should detect conflicts on major renaming including duplicate names and self-rename")
    void shouldDetectConflictsOnMajorRenaming() {
        Long major1Id = createMajor(COMPUTER_MAJOR);
        Long major2Id = createMajor(ELECTRICAL_MAJOR);

        String renameToOtherJson = "{\"name\": \"%s\"}".formatted(ELECTRICAL_MAJOR);
        ResponseEntity<String> conflictOther = restTemplate.exchange(
                String.format(MAJOR_NAME_URL, major1Id),
                HttpMethod.PUT,
                createJsonEntity(renameToOtherJson),
                String.class
        );
        assertThat(conflictOther.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        String renameToSelfJson = "{\"name\": \"%s\"}".formatted(COMPUTER_MAJOR);
        ResponseEntity<String> conflictSelf = restTemplate.exchange(
                String.format(MAJOR_NAME_URL, major1Id),
                HttpMethod.PUT,
                createJsonEntity(renameToSelfJson),
                String.class
        );
        assertThat(conflictSelf.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        String renameToNewJson = "{\"name\": \"%s\"}".formatted(AI_MAJOR);
        ResponseEntity<Void> renameSuccess = restTemplate.exchange(
                String.format(MAJOR_NAME_URL, major1Id),
                HttpMethod.PUT,
                createJsonEntity(renameToNewJson),
                Void.class
        );
        assertThat(renameSuccess.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should reject adding duplicate course to major with 422 Unprocessable Entity")
    void shouldRejectAddingDuplicateCourseToMajor() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long courseId = createCourse("Databases", "CS204", 3, List.of(majorId));

        ResponseEntity<String> duplicateAddResponse = restTemplate.postForEntity(
                String.format(MAJOR_COURSE_URL, majorId, courseId),
                null,
                String.class
        );

        assertThat(duplicateAddResponse.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
    }

    @Test
    @DisplayName("Should detect circular prerequisite deadlock preventing enrollment in both courses")
    void shouldDetectCircularPrerequisiteDeadlock() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long courseAId = createCourse("Course Alpha", "CA101", 3, List.of(majorId));
        Long courseBId = createCourse("Course Beta", "CB102", 3, List.of(majorId));

        updatePrerequisites(courseBId, List.of(courseAId), HttpStatus.NO_CONTENT);
        updatePrerequisites(courseAId, List.of(courseBId), HttpStatus.NO_CONTENT);

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        Long offeringAId = createOffering(courseAId, semesterId, CAPACITY);
        Long offeringBId = createOffering(courseBId, semesterId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offeringAId), HttpStatus.UNPROCESSABLE_CONTENT);
        enroll(studentId, List.of(offeringBId), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("Should reject duplicate IDs in major and prerequisite collections with proper validation codes")
    void shouldRejectDuplicateIdsInPayloadCollections() {
        Long majorId = createMajor(COMPUTER_MAJOR);

        String duplicateMajorsJson = """
                {
                    "name": "Software Engineering",
                    "courseCode": "SE100",
                    "credits": 3,
                    "majorIds": [%d, %d],
                    "prerequisiteIds": []
                }
                """.formatted(majorId, majorId);

        ResponseEntity<String> duplicateMajorsResponse = restTemplate.postForEntity(
                COURSES_URL,
                createJsonEntity(duplicateMajorsJson),
                String.class
        );
        assertThat(duplicateMajorsResponse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        Long course1Id = createCourse("Math 1", "MTH101", 3, List.of(majorId));
        Long course2Id = createCourse("Math 2", "MTH102", 3, List.of(majorId));

        String duplicatePrereqsJson = """
                {
                    "prerequisiteIds": [%d, %d]
                }
                """.formatted(course1Id, course1Id);

        ResponseEntity<String> duplicatePrereqsResponse = restTemplate.exchange(
                String.format(COURSE_PREREQUISITES_URL, course2Id),
                HttpMethod.PUT,
                createJsonEntity(duplicatePrereqsJson),
                String.class
        );
        assertThat(duplicatePrereqsResponse.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Should prevent new offerings and major additions for soft deleted course")
    void shouldPreventOfferingsForArchivedCourse() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long otherMajorId = createMajor(INDUSTRIAL_MAJOR);
        Long courseId = createCourse("Compiler Design", "CS401", 3, List.of(majorId));

        ResponseEntity<Void> archiveResponse = restTemplate.exchange(
                String.format(COURSE_ARCHIVE_URL, courseId),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(archiveResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetMajorCatalogResponse> catalogResponse = restTemplate.getForEntity(
                String.format(MAJOR_CATALOG_URL, majorId),
                GetMajorCatalogResponse.class
        );
        assertThat(catalogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(catalogResponse.getBody()).isNotNull();
        assertThat(catalogResponse.getBody().courses()).noneMatch(c -> c.courseId().equals(courseId));

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        String offeringJson = """
                {
                    "courseId": %d,
                    "semesterId": %d,
                    "professorName": "%s",
                    "section": "%s",
                    "guestAllowed": true,
                    "capacity": %d,
                    "classTime": "%s",
                    "examDate": "%s"
                }
                """.formatted(courseId, semesterId, PROFESSOR, SECTION, CAPACITY, CLASS_TIME, EXAM_DATE);

        ResponseEntity<String> createOfferingResponse = restTemplate.postForEntity(
                OFFERINGS_URL,
                createJsonEntity(offeringJson),
                String.class
        );
        assertThat(createOfferingResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> addArchivedCourseResponse = restTemplate.postForEntity(
                String.format(MAJOR_COURSE_URL, otherMajorId, courseId),
                null,
                String.class
        );
        assertThat(addArchivedCourseResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should adapt enrollment dynamically when prerequisites are added and subsequently removed")
    void shouldAdaptEnrollmentWhenPrerequisitesAreMutated() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long basicCourseId = createCourse("Basics", "BSC101", 3, List.of(majorId));
        Long advancedCourseId = createCourse("Advanced", "ADV201", 3, List.of(majorId));

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        Long advancedOfferingId = createOffering(advancedCourseId, semesterId, CAPACITY);

        Long student1Id = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        Long student2Id = registerStudent(STUDENT_NAME_2, STUDENT_NUMBER_2, majorId, EnrollmentPlanType.REGULAR);

        enroll(student1Id, List.of(advancedOfferingId), HttpStatus.OK);

        updatePrerequisites(advancedCourseId, List.of(basicCourseId), HttpStatus.NO_CONTENT);

        enroll(student2Id, List.of(advancedOfferingId), HttpStatus.UNPROCESSABLE_CONTENT);

        updatePrerequisites(advancedCourseId, List.of(), HttpStatus.NO_CONTENT);

        enroll(student2Id, List.of(advancedOfferingId), HttpStatus.OK);
    }

    private Long createMajor(String name) {
        String json = "{\"name\": \"%s\"}".formatted(name);
        ResponseEntity<Long> response = restTemplate.postForEntity(MAJORS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createCourse(String name, String courseCode, int credits, List<Long> majorIds) {
        String json = """
                {
                    "name": "%s",
                    "courseCode": "%s",
                    "credits": %d,
                    "majorIds": %s,
                    "prerequisiteIds": []
                }
                """.formatted(name, courseCode, credits, majorIds.toString());
        ResponseEntity<Long> response = restTemplate.postForEntity(COURSES_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createSemester(String title, String startDate, String endDate) {
        String json = """
                {
                    "title": "%s",
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """.formatted(title, startDate, endDate);
        ResponseEntity<Long> response = restTemplate.postForEntity(SEMESTERS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createOffering(Long courseId, Long semesterId, int capacity) {
        String json = """
                {
                    "courseId": %d,
                    "semesterId": %d,
                    "professorName": "%s",
                    "section": "%s",
                    "guestAllowed": true,
                    "capacity": %d,
                    "classTime": "%s",
                    "examDate": "%s"
                }
                """.formatted(courseId, semesterId, PROFESSOR, SECTION, capacity, CLASS_TIME, EXAM_DATE);
        ResponseEntity<Long> response = restTemplate.postForEntity(OFFERINGS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long registerStudent(String fullName, String studentNumber, Long majorId, EnrollmentPlanType planType) {
        String json = """
                {
                    "fullName": "%s",
                    "studentNumber": "%s",
                    "majorId": %d,
                    "planType": "%s"
                }
                """.formatted(fullName, studentNumber, majorId, planType.name());
        ResponseEntity<Long> response = restTemplate.postForEntity(STUDENTS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private void updatePrerequisites(Long courseId, List<Long> prereqIds, HttpStatus expectedStatus) {
        String json = """
                {
                    "prerequisiteIds": %s
                }
                """.formatted(prereqIds.toString());
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(COURSE_PREREQUISITES_URL, courseId),
                HttpMethod.PUT,
                createJsonEntity(json),
                Void.class
        );
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus.value());
    }

    private void enroll(Long studentId, List<Long> offeringIds, HttpStatus expectedStatus) {
        ResponseEntity<String> response = null;
        for (Long offeringId : offeringIds) {
            String json = """
                    {
                        "studentId": %d,
                        "courseOfferingId": %d
                    }
                    """.formatted(studentId, offeringId);
            response = restTemplate.postForEntity(ENROLLMENTS_URL, createJsonEntity(json), String.class);
            if (response.getStatusCode().value() != HttpStatus.OK.value())
                break;
        }
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).as(response.getBody()).isEqualTo(expectedStatus.value());
    }
}
