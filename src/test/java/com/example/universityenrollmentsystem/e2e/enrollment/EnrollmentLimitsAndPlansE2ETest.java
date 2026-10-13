package com.example.universityenrollmentsystem.e2e.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesResponse;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentLimitsAndPlansE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String GUEST_POLICY_URL = "/api/v1/course-offerings/%d/guest-policy";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/%d/students/%d";
    private static final String ELIGIBLE_URL = "/api/v1/enrollments/eligible?studentId=%d&semesterId=%d";
    private static final String STUDENTS_URL = "/api/v1/students";

    private static final String COMPUTER_MAJOR = "Computer Science";
    private static final String INDUSTRIAL_MAJOR = "Industrial Engineering";

    private static final String STUDENT_NAME_1 = "Mohsen Alipour";
    private static final String STUDENT_NUMBER_1 = "401777001";
    private static final String STUDENT_NAME_2 = "Farhad Taheri";
    private static final String STUDENT_NUMBER_2 = "401777002";

    private static final String PROFESSOR = "Dr. Mansoori";
    private static final String SECTION = "01";
    private static final int DEFAULT_CAPACITY = 30;
    private static final String CLASS_TIME = "Mon 14:00";
    private static final String EXAM_DATE = "2027-01-18";

    @Test
    @DisplayName("Should enforce exact 24 credit boundary on regular plan and allow recovery after drop")
    void shouldEnforceRegularPlan24CreditBoundary() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long c1 = createCourse("C1", "CS101", 4, List.of(majorId));
        Long c2 = createCourse("C2", "CS102", 4, List.of(majorId));
        Long c3 = createCourse("C3", "CS103", 4, List.of(majorId));
        Long c4 = createCourse("C4", "CS104", 4, List.of(majorId));
        Long c5 = createCourse("C5", "CS105", 4, List.of(majorId));
        Long c6 = createCourse("C6", "CS106", 4, List.of(majorId));
        Long cExtra = createCourse("Extra", "CS107", 2, List.of(majorId));

        Long off1 = createOffering(c1, semId, DEFAULT_CAPACITY, true);
        Long off2 = createOffering(c2, semId, DEFAULT_CAPACITY, true);
        Long off3 = createOffering(c3, semId, DEFAULT_CAPACITY, true);
        Long off4 = createOffering(c4, semId, DEFAULT_CAPACITY, true);
        Long off5 = createOffering(c5, semId, DEFAULT_CAPACITY, true);
        Long off6 = createOffering(c6, semId, DEFAULT_CAPACITY, true);
        Long offExtra = createOffering(cExtra, semId, DEFAULT_CAPACITY, true);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(off1, off2, off3, off4, off5, off6), HttpStatus.OK);

        enroll(studentId, List.of(offExtra), HttpStatus.UNPROCESSABLE_CONTENT);

        dropOffering(off6, studentId, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offExtra), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should enforce exact 14 credit boundary on probation plan")
    void shouldEnforceProbationPlan14CreditBoundary() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long c1 = createCourse("P1", "PRB101", 4, List.of(majorId));
        Long c2 = createCourse("P2", "PRB102", 4, List.of(majorId));
        Long c3 = createCourse("P3", "PRB103", 4, List.of(majorId));
        Long c3Credit = createCourse("P3Cred", "PRB104", 3, List.of(majorId));
        Long c2Credit = createCourse("P2Cred", "PRB105", 2, List.of(majorId));

        Long off1 = createOffering(c1, semId, DEFAULT_CAPACITY, true);
        Long off2 = createOffering(c2, semId, DEFAULT_CAPACITY, true);
        Long off3 = createOffering(c3, semId, DEFAULT_CAPACITY, true);
        Long off3Cred = createOffering(c3Credit, semId, DEFAULT_CAPACITY, true);
        Long off2Cred = createOffering(c2Credit, semId, DEFAULT_CAPACITY, true);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.PROBATION);

        enroll(studentId, List.of(off1, off2, off3), HttpStatus.OK);

        enroll(studentId, List.of(off3Cred), HttpStatus.UNPROCESSABLE_CONTENT);

        enroll(studentId, List.of(off2Cred), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should enforce honors plan outside major course limit per semester across multiple semesters")
    void shouldEnforceHonorsOutsideMajorLimitPerSemester() {
        Long compMajorId = createMajor(COMPUTER_MAJOR);
        Long indMajorId = createMajor(INDUSTRIAL_MAJOR);

        Long indCourse1 = createCourse("Project Mgmt", "IE101", 3, List.of(indMajorId));
        Long indCourse2 = createCourse("Quality Control", "IE102", 3, List.of(indMajorId));
        Long indCourse3 = createCourse("Supply Chain", "IE103", 3, List.of(indMajorId));

        Long sem1Id = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long sem2Id = createSemester("Spring 2027", "2027-02-10", "2027-06-20");

        Long offInd1Sem1 = createOffering(indCourse1, sem1Id, DEFAULT_CAPACITY, true);
        Long offInd2Sem1 = createOffering(indCourse2, sem1Id, DEFAULT_CAPACITY, true);
        Long offInd3Sem2 = createOffering(indCourse3, sem2Id, DEFAULT_CAPACITY, true);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, compMajorId, EnrollmentPlanType.HONORS);

        enroll(studentId, List.of(offInd1Sem1), HttpStatus.OK);

        enroll(studentId, List.of(offInd2Sem1), HttpStatus.UNPROCESSABLE_CONTENT);

        dropOffering(offInd1Sem1, studentId, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offInd2Sem1), HttpStatus.OK);

        enroll(studentId, List.of(offInd3Sem2), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should reflect dynamic guest policy modifications on eligibility and enrollment")
    void shouldReflectDynamicGuestPolicyModifications() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long courseId = createCourse("Networks", "NET101", 3, List.of(majorId));

        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long offeringId = createOffering(courseId, semId, DEFAULT_CAPACITY, false);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.GUEST);

        ResponseEntity<GetEligibleCoursesResponse> eligibleBefore = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, semId),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleBefore.getBody()).isNotNull();
        assertThat(eligibleBefore.getBody().eligibleOfferings()).noneMatch(c -> c.courseId().equals(courseId));

        enroll(studentId, List.of(offeringId), HttpStatus.UNPROCESSABLE_CONTENT);

        String guestPolicyJson = "{\"guestAllowed\": true}";
        ResponseEntity<Void> updatePolicyResponse = restTemplate.exchange(
                String.format(GUEST_POLICY_URL, offeringId),
                HttpMethod.PUT,
                createJsonEntity(guestPolicyJson),
                Void.class
        );
        assertThat(updatePolicyResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetEligibleCoursesResponse> eligibleAfter = restTemplate.getForEntity(
                String.format(ELIGIBLE_URL, studentId, semId),
                GetEligibleCoursesResponse.class
        );
        assertThat(eligibleAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eligibleAfter.getBody()).isNotNull();
        assertThat(eligibleAfter.getBody().eligibleOfferings()).anyMatch(c -> c.courseId().equals(courseId));

        enroll(studentId, List.of(offeringId), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should isolate credit calculation across parallel active semesters")
    void shouldIsolateCreditCalculationAcrossParallelSemesters() {
        Long majorId = createMajor(COMPUTER_MAJOR);

        Long semFallId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");
        Long semSummerId = createSemester("Summer 2027", "2027-07-01", "2027-08-30");

        Long c1 = createCourse("F1", "CS211", 4, List.of(majorId));
        Long c2 = createCourse("F2", "CS212", 4, List.of(majorId));
        Long c3 = createCourse("F3", "CS213", 4, List.of(majorId));
        Long c4 = createCourse("F4", "CS214", 4, List.of(majorId));
        Long c5 = createCourse("F5", "CS215", 4, List.of(majorId));
        Long c6 = createCourse("F6", "CS216", 4, List.of(majorId));
        Long cFallOver = createCourse("FOver", "CS217", 4, List.of(majorId));

        Long s1 = createCourse("S1", "SUM101", 4, List.of(majorId));
        Long s2 = createCourse("S2", "SUM102", 4, List.of(majorId));

        Long offF1 = createOffering(c1, semFallId, DEFAULT_CAPACITY, true);
        Long offF2 = createOffering(c2, semFallId, DEFAULT_CAPACITY, true);
        Long offF3 = createOffering(c3, semFallId, DEFAULT_CAPACITY, true);
        Long offF4 = createOffering(c4, semFallId, DEFAULT_CAPACITY, true);
        Long offF5 = createOffering(c5, semFallId, DEFAULT_CAPACITY, true);
        Long offF6 = createOffering(c6, semFallId, DEFAULT_CAPACITY, true);
        Long offFOver = createOffering(cFallOver, semFallId, DEFAULT_CAPACITY, true);

        Long offS1 = createOffering(s1, semSummerId, DEFAULT_CAPACITY, true);
        Long offS2 = createOffering(s2, semSummerId, DEFAULT_CAPACITY, true);

        Long studentId = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offF1, offF2, offF3, offF4, offF5, offF6), HttpStatus.OK);

        enroll(studentId, List.of(offS1), HttpStatus.OK);
        enroll(studentId, List.of(offS2), HttpStatus.OK);

        enroll(studentId, List.of(offFOver), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("Should prevent course swap exceeding capacity and manage capacity race conditions")
    void shouldManageCourseSwappingAtMaxCapacity() {
        Long majorId = createMajor(COMPUTER_MAJOR);
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long c1 = createCourse("Major1", "CS311", 4, List.of(majorId));
        Long c2 = createCourse("Major2", "CS312", 4, List.of(majorId));
        Long c3 = createCourse("Major3", "CS313", 4, List.of(majorId));
        Long c4 = createCourse("Major4", "CS314", 4, List.of(majorId));
        Long c5 = createCourse("Major5", "CS315", 4, List.of(majorId));
        Long cDrop = createCourse("MajorDrop", "CS316", 4, List.of(majorId));
        Long cTarget = createCourse("TargetCourse", "CS317", 4, List.of(majorId));

        Long off1 = createOffering(c1, semId, DEFAULT_CAPACITY, true);
        Long off2 = createOffering(c2, semId, DEFAULT_CAPACITY, true);
        Long off3 = createOffering(c3, semId, DEFAULT_CAPACITY, true);
        Long off4 = createOffering(c4, semId, DEFAULT_CAPACITY, true);
        Long off5 = createOffering(c5, semId, DEFAULT_CAPACITY, true);
        Long offDrop = createOffering(cDrop, semId, DEFAULT_CAPACITY, true);
        Long offTarget = createOffering(cTarget, semId, 1, true);

        Long student1Id = registerStudent(STUDENT_NAME_1, STUDENT_NUMBER_1, majorId, EnrollmentPlanType.REGULAR);
        Long student2Id = registerStudent(STUDENT_NAME_2, STUDENT_NUMBER_2, majorId, EnrollmentPlanType.REGULAR);

        enroll(student1Id, List.of(off1, off2, off3, off4, off5, offDrop), HttpStatus.OK);

        enroll(student1Id, List.of(offTarget), HttpStatus.UNPROCESSABLE_CONTENT);

        dropOffering(offDrop, student1Id, HttpStatus.NO_CONTENT);

        enroll(student2Id, List.of(offTarget), HttpStatus.OK);

        enroll(student1Id, List.of(offTarget), HttpStatus.CONFLICT);

        enroll(student1Id, List.of(offDrop), HttpStatus.OK);
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

    private Long createOffering(Long courseId, Long semesterId, int capacity, boolean guestAllowed) {
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
                    "guestAllowed": %b,
                    "capacity": %d,
                    "classTime": "%s",
                    "examDate": "%s"
                }
                """.formatted(courseId, semesterId, PROFESSOR, SECTION, guestAllowed, capacity, CLASS_TIME, examDate);
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
