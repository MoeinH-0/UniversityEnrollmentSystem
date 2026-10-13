package com.example.universityenrollmentsystem.e2e.grading;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsResponse;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GradingAndTranscriptE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String COURSE_CREDITS_URL = "/api/v1/courses/%d/credits";
    private static final String COURSE_STATS_URL = "/api/v1/courses/%d/statistics";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String OFFERING_GRADE_URL = "/api/v1/course-offerings/%d/grades";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/%d/students/%d";
    private static final String STUDENTS_URL = "/api/v1/students";
    private static final String TRANSCRIPT_URL = "/api/v1/students/%d/transcript";

    private static final String MAJOR_CS = "Computer Science";
    private static final String MAJOR_EE = "Electrical Engineering";

    private static final String STUDENT_NAME_1 = "Navid Mohammadi";
    private static final String STUDENT_NUMBER_1 = "401333001";
    private static final String STUDENT_NAME_2 = "Saman Karimi";
    private static final String STUDENT_NUMBER_2 = "401333002";
    private static final String STUDENT_NAME_3 = "Arman Moradi";
    private static final String STUDENT_NUMBER_3 = "401333003";
    private static final String STUDENT_NAME_4 = "Danial Abbasi";
    private static final String STUDENT_NUMBER_4 = "401333004";

    private static final String PROFESSOR = "Dr. Tavakoli";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Sun 10:00";
    private static final String EXAM_DATE = "2027-01-14";

    @Test
    @DisplayName("Should lock course after grading preventing re-grading and course drop")
    void shouldLockCourseAfterGradingPreventingReGradingAndDrop() {
        Long majorId = createMajor(MAJOR_CS);
        Long courseId = createCourse("Calculus II", "MTH201", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offeringId = createOffering(courseId, semId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(offeringId), HttpStatus.OK);

        submitGrade(offeringId, studentId, 17.50, HttpStatus.NO_CONTENT);

        submitGrade(offeringId, studentId, 19.00, HttpStatus.UNPROCESSABLE_CONTENT);

        dropOffering(offeringId, studentId, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("Should calculate weighted GPA with failed courses and HALF_UP rounding correctly")
    void shouldCalculateWeightedGpaWithFailedCoursesAndRounding() {
        Long majorId = createMajor(MAJOR_CS);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long courseA = createCourse("Course A", "CSA101", 3, List.of(majorId));
        Long courseB = createCourse("Course B", "CSB102", 2, List.of(majorId));
        Long courseC = createCourse("Course C", "CSC103", 4, List.of(majorId));

        Long offA = createOffering(courseA, semId, CAPACITY);
        Long offB = createOffering(courseB, semId, CAPACITY);
        Long offC = createOffering(courseC, semId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(offA, offB, offC), HttpStatus.OK);

        submitGrade(offA, studentId, 15.00, HttpStatus.NO_CONTENT);
        submitGrade(offB, studentId, 18.50, HttpStatus.NO_CONTENT);
        submitGrade(offC, studentId, 8.00, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );

        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcript.getBody()).isNotNull();
        assertThat(transcript.getBody().totalPassedCredits()).isEqualTo(5);
        assertThat(transcript.getBody().gpa()).isEqualTo(12.67);

        ResponseEntity<GetCourseStatisticsResponse> stats = restTemplate.getForEntity(
                String.format(COURSE_STATS_URL, courseC),
                GetCourseStatisticsResponse.class
        );
        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).isNotNull();
        assertThat(stats.getBody().overall().totalStudentsFailed()).isEqualTo(1);
        assertThat(stats.getBody().overall().averageGrade()).isEqualTo(8.00);
    }

    @Test
    @DisplayName("Should update transcript credits and recalculate when course credits are modified")
    void shouldReflectCourseCreditsModificationOnTranscript() {
        Long majorId = createMajor(MAJOR_CS);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long courseId = createCourse("Physics", "PHY110", 3, List.of(majorId));
        Long offeringId = createOffering(courseId, semId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(offeringId), HttpStatus.OK);
        submitGrade(offeringId, studentId, 16.00, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcriptBefore = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );
        assertThat(transcriptBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcriptBefore.getBody()).isNotNull();
        assertThat(transcriptBefore.getBody().totalPassedCredits()).isEqualTo(3);
        assertThat(transcriptBefore.getBody().gpa()).isEqualTo(16.00);

        String updateCreditsJson = "{\"credits\": 4}";
        ResponseEntity<Void> updateCreditsResponse = restTemplate.exchange(
                String.format(COURSE_CREDITS_URL, courseId),
                HttpMethod.PUT,
                createJsonEntity(updateCreditsJson),
                Void.class
        );
        assertThat(updateCreditsResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcriptAfter = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );
        assertThat(transcriptAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcriptAfter.getBody()).isNotNull();
        assertThat(transcriptAfter.getBody().totalPassedCredits()).isEqualTo(4);
        assertThat(transcriptAfter.getBody().gpa()).isEqualTo(16.00);
    }

    @Test
    @DisplayName("Should exclude null grades and in-progress students from course statistics average")
    void shouldExcludeNullGradesAndInProgressFromStatisticsAverage() {
        Long csMajorId = createMajor(MAJOR_CS);
        Long eeMajorId = createMajor(MAJOR_EE);

        Long physicsCourseId = createCourse("Physics II", "PHY200", 3, List.of(csMajorId, eeMajorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offeringId = createOffering(physicsCourseId, semId, CAPACITY);

        Long eeStudent1 = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, eeMajorId, EnrollmentPlanType.REGULAR);
        Long eeStudent2 = registerStudent(STUDENT_NAME_2, STUDENT_NUMBER_2, eeMajorId, EnrollmentPlanType.REGULAR);
        Long csStudent1 = registerStudent(STUDENT_NAME_3, STUDENT_NUMBER_3, csMajorId, EnrollmentPlanType.REGULAR);
        Long csStudent2 = registerStudent(STUDENT_NAME_4, STUDENT_NUMBER_4, csMajorId, EnrollmentPlanType.REGULAR);

        enroll(eeStudent1, List.of(offeringId), HttpStatus.OK);
        enroll(eeStudent2, List.of(offeringId), HttpStatus.OK);
        enroll(csStudent1, List.of(offeringId), HttpStatus.OK);
        enroll(csStudent2, List.of(offeringId), HttpStatus.OK);

        submitGrade(offeringId, eeStudent1, 20.00, HttpStatus.NO_CONTENT);
        submitGrade(offeringId, eeStudent2, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(offeringId, csStudent1, 8.00, HttpStatus.NO_CONTENT);

        ResponseEntity<GetCourseStatisticsResponse> stats = restTemplate.getForEntity(
                String.format(COURSE_STATS_URL, physicsCourseId),
                GetCourseStatisticsResponse.class
        );

        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).isNotNull();
        assertThat(stats.getBody().overall().totalStudentsPassed()).isEqualTo(2);
        assertThat(stats.getBody().overall().totalStudentsFailed()).isEqualTo(1);
        assertThat(stats.getBody().overall().totalStudentsInProgress()).isEqualTo(1);
        assertThat(stats.getBody().overall().averageGrade()).isEqualTo(12.67);

        assertThat(stats.getBody().byMajor()).anyMatch(m ->
                m.majorName().equals(MAJOR_EE) && m.totalStudentsPassed() == 2 && m.averageGrade() == 15.00
        );
        assertThat(stats.getBody().byMajor()).anyMatch(m ->
                m.majorName().equals(MAJOR_CS) && m.totalStudentsFailed() == 1 && m.totalStudentsInProgress() == 1 && m.averageGrade() == 8.00
        );
    }

    @Test
    @DisplayName("Should return blank transcript with zero GPA when all enrolled courses are dropped")
    void shouldReturnBlankTranscriptWhenAllCoursesDropped() {
        Long majorId = createMajor(MAJOR_CS);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long c1 = createCourse("Discrete", "CS150", 3, List.of(majorId));
        Long c2 = createCourse("Logic", "CS160", 3, List.of(majorId));

        Long off1 = createOffering(c1, semId, CAPACITY);
        Long off2 = createOffering(c2, semId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(off1, off2), HttpStatus.OK);

        dropOffering(off1, studentId, HttpStatus.NO_CONTENT);
        dropOffering(off2, studentId, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );

        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcript.getBody()).isNotNull();
        assertThat(transcript.getBody().gpa()).isEqualTo(0.0);
        assertThat(transcript.getBody().totalPassedCredits()).isEqualTo(0);
        assertThat(transcript.getBody().semesters()).isEmpty();
    }

    @Test
    @DisplayName("Should calculate weighted overall GPA distinguishing from simple semester GPA average")
    void shouldCalculateWeightedOverallGpaAcrossUnequalSemesters() {
        Long majorId = createMajor(MAJOR_CS);

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");

        Long s1c1 = createCourse("Sem1C1", "S110", 3, List.of(majorId));
        Long s1c2 = createCourse("Sem1C2", "S120", 3, List.of(majorId));

        Long s2c1 = createCourse("Sem2C1", "S210", 3, List.of(majorId));
        Long s2c2 = createCourse("Sem2C2", "S220", 3, List.of(majorId));
        Long s2c3 = createCourse("Sem2C3", "S230", 4, List.of(majorId));
        Long s2c4 = createCourse("Sem2C4", "S240", 4, List.of(majorId));
        Long s2c5 = createCourse("Sem2C5", "S250", 4, List.of(majorId));

        Long offS11 = createOffering(s1c1, sem1Id, CAPACITY);
        Long offS12 = createOffering(s1c2, sem1Id, CAPACITY);

        Long offS21 = createOffering(s2c1, sem2Id, CAPACITY);
        Long offS22 = createOffering(s2c2, sem2Id, CAPACITY);
        Long offS23 = createOffering(s2c3, sem2Id, CAPACITY);
        Long offS24 = createOffering(s2c4, sem2Id, CAPACITY);
        Long offS25 = createOffering(s2c5, sem2Id, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offS11, offS12), HttpStatus.OK);
        submitGrade(offS11, studentId, 20.00, HttpStatus.NO_CONTENT);
        submitGrade(offS12, studentId, 20.00, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offS21, offS22, offS23, offS24, offS25), HttpStatus.OK);
        submitGrade(offS21, studentId, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(offS22, studentId, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(offS23, studentId, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(offS24, studentId, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(offS25, studentId, 10.00, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );

        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        Double gpa1 = transcript.getBody().semesters().stream()
                .filter(s -> s.semesterId().equals(sem1Id))
                .findFirst()
                .map(GetStudentTranscriptResponse.SemesterTranscriptDto::semesterGpa)
                .orElse(0.0);
        Double gpa2 = transcript.getBody().semesters().stream()
                .filter(s -> s.semesterId().equals(sem2Id))
                .findFirst()
                .map(GetStudentTranscriptResponse.SemesterTranscriptDto::semesterGpa)
                .orElse(0.0);
        assertThat(gpa1).isEqualTo(20.00);
        assertThat(gpa2).isEqualTo(10.00);
        assertThat(transcript.getBody().gpa()).isEqualTo(12.50);
    }

    @Test
    @DisplayName("Should validate grade boundaries and decimal scale limits")
    void shouldValidateGradeBoundariesAndScaleLimits() {
        Long majorId = createMajor(MAJOR_CS);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long c1 = createCourse("C1", "G101", 3, List.of(majorId));
        Long c2 = createCourse("C2", "G102", 3, List.of(majorId));
        Long c3 = createCourse("C3", "G103", 3, List.of(majorId));
        Long c4 = createCourse("C4", "G104", 3, List.of(majorId));
        Long c5 = createCourse("C5", "G105", 3, List.of(majorId));

        Long off1 = createOffering(c1, semId, CAPACITY);
        Long off2 = createOffering(c2, semId, CAPACITY);
        Long off3 = createOffering(c3, semId, CAPACITY);
        Long off4 = createOffering(c4, semId, CAPACITY);
        Long off5 = createOffering(c5, semId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(off1, off2, off3, off4, off5), HttpStatus.OK);

        submitRawGrade(off3, studentId, "14.875", HttpStatus.BAD_REQUEST);
        submitRawGrade(off3, studentId, "-0.01", HttpStatus.BAD_REQUEST);
        submitRawGrade(off3, studentId, "20.01", HttpStatus.BAD_REQUEST);

        submitGrade(off1, studentId, 0.00, HttpStatus.NO_CONTENT);
        submitGrade(off2, studentId, 20.00, HttpStatus.NO_CONTENT);
        submitGrade(off4, studentId, 10.00, HttpStatus.NO_CONTENT);
        submitGrade(off5, studentId, 9.99, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcript = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );
        assertThat(transcript.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcript.getBody()).isNotNull();
        assertThat(transcript.getBody().semesters().get(0).courses())
                .anyMatch(c -> c.courseCode().equals("G101") && c.status().name().equals("FAILED"));
        assertThat(transcript.getBody().semesters().get(0).courses())
                .anyMatch(c -> c.courseCode().equals("G102") && c.status().name().equals("PASSED"));
        assertThat(transcript.getBody().semesters().get(0).courses())
                .anyMatch(c -> c.courseCode().equals("G104") && c.status().name().equals("PASSED"));
        assertThat(transcript.getBody().semesters().get(0).courses())
                .anyMatch(c -> c.courseCode().equals("G105") && c.status().name().equals("FAILED"));
    }

    @Test
    @DisplayName("Should round exact half way values to upper decimal point via HALF_UP mode")
    void shouldRoundExactHalfWayValuesToUpperDecimalPoint() {
        Long majorId = createMajor(MAJOR_CS);
        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");

        Long c1 = createCourse("H1", "HLF101", 1, List.of(majorId));
        Long c2 = createCourse("H2", "HLF102", 1, List.of(majorId));
        Long c3 = createCourse("H3", "HLF103", 1, List.of(majorId));
        Long c4 = createCourse("H4", "HLF104", 1, List.of(majorId));

        Long off1 = createOffering(c1, sem1Id, CAPACITY);
        Long off2 = createOffering(c2, sem1Id, CAPACITY);
        Long off3 = createOffering(c3, sem2Id, CAPACITY);
        Long off4 = createOffering(c4, sem2Id, CAPACITY);

        Long s1Id = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        enroll(s1Id, List.of(off1, off2), HttpStatus.OK);
        submitGrade(off1, s1Id, 10.05, HttpStatus.NO_CONTENT);
        submitGrade(off2, s1Id, 10.06, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> t1 = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, s1Id),
                GetStudentTranscriptResponse.class
        );
        assertThat(t1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(t1.getBody()).isNotNull();
        assertThat(t1.getBody().gpa()).isEqualTo(10.06);

        Long s2Id = registerStudent(STUDENT_NAME_2, STUDENT_NUMBER_2, majorId, EnrollmentPlanType.REGULAR);
        enroll(s2Id, List.of(off3, off4), HttpStatus.OK);
        submitGrade(off3, s2Id, 10.05, HttpStatus.NO_CONTENT);
        submitGrade(off4, s2Id, 10.04, HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> t2 = restTemplate.getForEntity(
                String.format(TRANSCRIPT_URL, s2Id),
                GetStudentTranscriptResponse.class
        );
        assertThat(t2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(t2.getBody()).isNotNull();
        assertThat(t2.getBody().gpa()).isEqualTo(10.05);
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

    private void submitRawGrade(Long offeringId, Long studentId, String rawGrade, HttpStatus expectedStatus) {
        String json = """
                {
                    "studentId": %d,
                    "grade": %s
                }
                """.formatted(studentId, rawGrade);
        ResponseEntity<String> response = restTemplate.exchange(
                String.format(OFFERING_GRADE_URL, offeringId),
                HttpMethod.PUT,
                createJsonEntity(json),
                String.class
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
