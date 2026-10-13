package com.example.universityenrollmentsystem.e2e.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesResponse;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrerequisiteProgressionE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String COURSE_PREREQS_URL = "/api/v1/courses/%d/prerequisites";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String OFFERING_GRADE_URL = "/api/v1/course-offerings/%d/grades";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/%d/students/%d";
    private static final String ELIGIBLE_URL = "/api/v1/enrollments/eligible?studentId=%d&semesterId=%d";
    private static final String STUDENTS_URL = "/api/v1/students";
    private static final String TRANSCRIPT_URL = "/api/v1/students/%d/transcript";

    private static final String MAJOR_NAME = "Computer Engineering";
    private static final String STUDENT_NAME = "Reza Tehrani";
    private static final String STUDENT_NUMBER = "401555666";
    private static final String PROFESSOR = "Dr. Alavi";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Sat 10:00";
    private static final String EXAM_DATE = "2027-01-10";

    @Test
    @DisplayName("Should unlock prerequisite dependent course only after status transitions to PASSED")
    void shouldUnlockPrerequisiteDependentCourseOnlyWhenPassed() {
        Long majorId = createMajor(MAJOR_NAME);
        Long course1Id = createCourse("Programming 101", "CS101", 3, List.of(majorId));
        Long course2Id = createCourse("Data Structures", "CS102", 3, List.of(majorId));
        updatePrerequisites(course2Id, List.of(course1Id));

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offering1Id = createOffering(course1Id, sem1Id, CAPACITY);
        Long offering2Sem1Id = createOffering(course2Id, sem1Id, CAPACITY);

        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");
        Long offering2Sem2Id = createOffering(course2Id, sem2Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offering2Sem1Id), HttpStatus.UNPROCESSABLE_CONTENT);

        enroll(studentId, List.of(offering1Id), HttpStatus.OK);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem2BeforeGrade = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem2Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem2BeforeGrade.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem2BeforeGrade.getBody()).isNotNull();
        assertThat(eligibleSem2BeforeGrade.getBody().eligibleOfferings())
                .noneMatch(c -> c.courseId().equals(course2Id));

        submitGrade(offering1Id, studentId, 9.75, HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem2AfterFail = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem2Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem2AfterFail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem2AfterFail.getBody()).isNotNull();
        assertThat(eligibleSem2AfterFail.getBody().eligibleOfferings())
                .noneMatch(c -> c.courseId().equals(course2Id));

        Long offering1Sem2Id = createOffering(course1Id, sem2Id, CAPACITY);
        enroll(studentId, List.of(offering1Sem2Id), HttpStatus.OK);
        submitGrade(offering1Sem2Id, studentId, 14.50, HttpStatus.NO_CONTENT);

        Long sem3Id = createSemester("Fall 2027", "2027-09-20", "2028-01-20");
        Long offering2Sem3Id = createOffering(course2Id, sem3Id, CAPACITY);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem3AfterPass = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem3Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem3AfterPass.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem3AfterPass.getBody()).isNotNull();
        assertThat(eligibleSem3AfterPass.getBody().eligibleOfferings())
                .anyMatch(c -> c.courseId().equals(course2Id));

        enroll(studentId, List.of(offering2Sem3Id), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should enforce retake policy permitting failed course to be retaken while separating transcript records")
    void shouldEnforceRetakePolicyWithDistinctSemesterTranscriptEntries() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Calculus", "MTH101", 3, List.of(majorId));

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offering1Id = createOffering(courseId, sem1Id, CAPACITY);

        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");
        Long offering2Id = createOffering(courseId, sem2Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offering1Id), HttpStatus.OK);

        enroll(studentId, List.of(offering1Id), HttpStatus.CONFLICT);

        submitGrade(offering1Id, studentId, 8.0, HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem2 = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem2Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem2.getBody()).isNotNull();
        assertThat(eligibleSem2.getBody().eligibleOfferings())
                .anyMatch(c -> c.courseId().equals(courseId));

        enroll(studentId, List.of(offering2Id), HttpStatus.OK);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );
        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcript.getBody()).isNotNull();
        assertThat(transcript.getBody().semesters()).hasSize(2);
        boolean hasFailed = transcript.getBody().semesters().stream()
                .anyMatch(s -> s.semesterTitle().equals("Fall 2026") && s.courses().get(0).status().name().equals("FAILED"));
        boolean hasInProgress = transcript.getBody().semesters().stream()
                .anyMatch(s -> s.semesterTitle().equals("Spring 2027") && s.courses().get(0).status().name().equals("IN_PROGRESS"));
        assertThat(hasFailed).isTrue();
        assertThat(hasInProgress).isTrue();
    }

    @Test
    @DisplayName("Should handle diamond prerequisite graph locking final course until all branches pass")
    void shouldHandleDiamondPrerequisiteGraphLifecycle() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseA = createCourse("Basics A", "CS100", 3, List.of(majorId));
        Long courseB = createCourse("Branch B", "CS201", 3, List.of(majorId));
        Long courseC = createCourse("Branch C", "CS202", 3, List.of(majorId));
        Long courseD = createCourse("Target D", "CS301", 3, List.of(majorId));

        updatePrerequisites(courseB, List.of(courseA));
        updatePrerequisites(courseC, List.of(courseA));
        updatePrerequisites(courseD, List.of(courseB, courseC));

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offA = createOffering(courseA, sem1Id, CAPACITY);

        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");
        Long offB = createOffering(courseB, sem2Id, CAPACITY);
        Long offC = createOffering(courseC, sem2Id, CAPACITY);

        Long sem3Id = createSemester("Fall 2027", "2027-09-20", "2028-01-20");
        Long offD = createOffering(courseD, sem3Id, CAPACITY);
        Long offCRepeat = createOffering(courseC, sem3Id, CAPACITY);

        Long sem4Id = createSemester("Spring 2028", "2028-02-10", "2028-06-20");
        Long offDFinal = createOffering(courseD, sem4Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offA), HttpStatus.OK);
        submitGrade(offA, studentId, 16.0, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offB, offC), HttpStatus.OK);
        submitGrade(offB, studentId, 15.0, HttpStatus.NO_CONTENT);
        submitGrade(offC, studentId, 8.5, HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem3 = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem3Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem3.getBody()).isNotNull();
        assertThat(eligibleSem3.getBody().eligibleOfferings()).noneMatch(c -> c.courseId().equals(courseD));

        enroll(studentId, List.of(offD), HttpStatus.UNPROCESSABLE_CONTENT);

        enroll(studentId, List.of(offCRepeat), HttpStatus.OK);
        submitGrade(offCRepeat, studentId, 12.0, HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem4 = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem4Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem4.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem4.getBody()).isNotNull();
        assertThat(eligibleSem4.getBody().eligibleOfferings()).anyMatch(c -> c.courseId().equals(courseD));

        enroll(studentId, List.of(offDFinal), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should validate prerequisites across non-consecutive semesters with gaps and drops")
    void shouldValidatePrerequisitesAcrossNonConsecutiveSemesters() {
        Long majorId = createMajor(MAJOR_NAME);
        Long math1 = createCourse("Math 1", "MTH111", 3, List.of(majorId));
        Long math2 = createCourse("Math 2", "MTH222", 3, List.of(majorId));
        Long elective = createCourse("Art", "ART101", 2, List.of(majorId));
        updatePrerequisites(math2, List.of(math1));

        Long sem1Id = createSemester("Fall 2024", "2024-09-20", "2025-01-20");
        Long offMath1 = createOffering(math1, sem1Id, CAPACITY);

        Long sem3Id = createSemester("Fall 2025", "2025-09-20", "2026-01-20");
        Long offArt = createOffering(elective, sem3Id, CAPACITY);

        Long sem4Id = createSemester("Spring 2026", "2026-02-10", "2026-06-20");
        Long offMath2 = createOffering(math2, sem4Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offMath1), HttpStatus.OK);
        submitGrade(offMath1, studentId, 15.0, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offArt), HttpStatus.OK);
        dropOffering(offArt, studentId, HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleSem4 = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, sem4Id),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleSem4.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleSem4.getBody()).isNotNull();
        assertThat(eligibleSem4.getBody().eligibleOfferings()).anyMatch(c -> c.courseId().equals(math2));

        enroll(studentId, List.of(offMath2), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should prevent dropping a passed course preserving prerequisite immutability")
    void shouldPreventDroppingPassedCourse() {
        Long majorId = createMajor(MAJOR_NAME);
        Long course1Id = createCourse("Foundation", "FND101", 3, List.of(majorId));
        Long course2Id = createCourse("Specialty", "SPC201", 3, List.of(majorId));
        updatePrerequisites(course2Id, List.of(course1Id));

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long off1 = createOffering(course1Id, sem1Id, CAPACITY);

        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");
        Long off2 = createOffering(course2Id, sem2Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(off1), HttpStatus.OK);
        submitGrade(off1, studentId, 14.0, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(off2), HttpStatus.OK);

        dropOffering(off1, studentId, HttpStatus.UNPROCESSABLE_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );
        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        String courseStatus = transcript.getBody().semesters().stream()
                .filter(s -> s.semesterId().equals(sem1Id))
                .flatMap(s -> s.courses().stream())
                .findFirst()
                .map(c -> c.status().name())
                .orElse("");
        assertThat(courseStatus).isEqualTo("PASSED");
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
        GetSemesterDetailsResponse sem = restTemplate.getForEntity(
                "/api/v1/semesters/" + semesterId,
                GetSemesterDetailsResponse.class
        ).getBody();
        String examDate = (sem != null) ? sem.endDate().minusDays(5).toString() : EXAM_DATE;

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
                """.formatted(courseId, semesterId, PROFESSOR, SECTION, capacity, CLASS_TIME, examDate);
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

    private void updatePrerequisites(Long courseId, List<Long> prereqIds) {
        String json = "{\"prerequisiteIds\": %s}".formatted(prereqIds.toString());
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(COURSE_PREREQS_URL, courseId),
                HttpMethod.PUT,
                createJsonEntity(json),
                Void.class
        );
        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private void submitGrade(Long offeringId, Long studentId, Double grade, HttpStatus expectedStatus) {
        String json = """
                {
                    "studentId": %d,
                    "grade": %.2f
                }
                """.formatted(studentId, grade);
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(OFFERING_GRADE_URL, offeringId),
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

    private void dropOffering(Long offeringId, Long studentId, HttpStatus expectedStatus) {
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(DROP_URL, offeringId, studentId),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus.value());
    }
}
