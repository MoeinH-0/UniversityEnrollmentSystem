package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.service.courseOfferings.cancel.CancelCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.cancel.CancelCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.capacity.ChangeCourseOfferingCapacityCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.capacity.ChangeCourseOfferingCapacityCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.create.CreateCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.create.CreateCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsQuery;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.details.GetCourseOfferingDetailsResponse;
import com.example.universityenrollmentsystem.service.courseOfferings.exam.RescheduleCourseOfferingExamCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.exam.RescheduleCourseOfferingExamCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.grade.SubmitStudentGradeCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.grade.SubmitStudentGradeCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy.ChangeCourseOfferingGuestPolicyCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy.ChangeCourseOfferingGuestPolicyCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.professor.ChangeCourseOfferingProfessorCommand;
import com.example.universityenrollmentsystem.service.courseOfferings.professor.ChangeCourseOfferingProfessorCommandHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsQuery;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsQueryHandler;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/course-offerings")
public class CourseOfferingController {

    private final CreateCourseOfferingCommandHandler createCourseOfferingCommandHandler;
    private final CancelCourseOfferingCommandHandler cancelCourseOfferingCommandHandler;
    private final ChangeCourseOfferingCapacityCommandHandler changeCourseOfferingCapacityCommandHandler;
    private final ChangeCourseOfferingProfessorCommandHandler changeCourseOfferingProfessorCommandHandler;
    private final ChangeCourseOfferingGuestPolicyCommandHandler changeCourseOfferingGuestPolicyCommandHandler;
    private final RescheduleCourseOfferingExamCommandHandler rescheduleCourseOfferingExamCommandHandler;
    private final SubmitStudentGradeCommandHandler submitStudentGradeCommandHandler;
    private final GetCourseOfferingDetailsQueryHandler getCourseOfferingDetailsQueryHandler;
    private final GetCourseOfferingStudentsQueryHandler getCourseOfferingStudentsQueryHandler;

    public CourseOfferingController(CreateCourseOfferingCommandHandler createCourseOfferingCommandHandler,
                                    CancelCourseOfferingCommandHandler cancelCourseOfferingCommandHandler,
                                    ChangeCourseOfferingCapacityCommandHandler changeCourseOfferingCapacityCommandHandler,
                                    ChangeCourseOfferingProfessorCommandHandler changeCourseOfferingProfessorCommandHandler,
                                    ChangeCourseOfferingGuestPolicyCommandHandler changeCourseOfferingGuestPolicyCommandHandler,
                                    RescheduleCourseOfferingExamCommandHandler rescheduleCourseOfferingExamCommandHandler,
                                    SubmitStudentGradeCommandHandler submitStudentGradeCommandHandler,
                                    GetCourseOfferingDetailsQueryHandler getCourseOfferingDetailsQueryHandler,
                                    GetCourseOfferingStudentsQueryHandler getCourseOfferingStudentsQueryHandler) {
        this.createCourseOfferingCommandHandler = createCourseOfferingCommandHandler;
        this.cancelCourseOfferingCommandHandler = cancelCourseOfferingCommandHandler;
        this.changeCourseOfferingCapacityCommandHandler = changeCourseOfferingCapacityCommandHandler;
        this.changeCourseOfferingProfessorCommandHandler = changeCourseOfferingProfessorCommandHandler;
        this.changeCourseOfferingGuestPolicyCommandHandler = changeCourseOfferingGuestPolicyCommandHandler;
        this.rescheduleCourseOfferingExamCommandHandler = rescheduleCourseOfferingExamCommandHandler;
        this.submitStudentGradeCommandHandler = submitStudentGradeCommandHandler;
        this.getCourseOfferingDetailsQueryHandler = getCourseOfferingDetailsQueryHandler;
        this.getCourseOfferingStudentsQueryHandler = getCourseOfferingStudentsQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Long> createCourseOffering(@RequestBody CreateCourseOfferingCommand command) {
        Long courseOfferingId = createCourseOfferingCommandHandler.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/course-offerings/" + courseOfferingId)).body(courseOfferingId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelCourseOffering(@PathVariable Long id) {
        cancelCourseOfferingCommandHandler.handle(new CancelCourseOfferingCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/capacity")
    public ResponseEntity<Void> changeCapacity(@PathVariable Long id, @RequestBody ChangeCapacityRequest request) {
        changeCourseOfferingCapacityCommandHandler.handle(new ChangeCourseOfferingCapacityCommand(id, request.capacity()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/professor")
    public ResponseEntity<Void> changeProfessor(@PathVariable Long id, @RequestBody ChangeProfessorRequest request) {
        changeCourseOfferingProfessorCommandHandler.handle(new ChangeCourseOfferingProfessorCommand(id, request.professorName()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/guest-policy")
    public ResponseEntity<Void> changeGuestPolicy(@PathVariable Long id, @RequestBody ChangeGuestPolicyRequest request) {
        changeCourseOfferingGuestPolicyCommandHandler.handle(new ChangeCourseOfferingGuestPolicyCommand(id, request.guestAllowed()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/exam-date")
    public ResponseEntity<Void> rescheduleExam(@PathVariable Long id, @RequestBody RescheduleExamRequest request) {
        rescheduleCourseOfferingExamCommandHandler.handle(new RescheduleCourseOfferingExamCommand(id, request.examDate()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/grades")
    public ResponseEntity<Void> submitGrade(@PathVariable Long id, @RequestBody SubmitGradeRequest request) {
        submitStudentGradeCommandHandler.handle(new SubmitStudentGradeCommand(request.studentId(), id, request.grade()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCourseOfferingDetailsResponse> getDetails(@PathVariable Long id) {
        GetCourseOfferingDetailsResponse response = getCourseOfferingDetailsQueryHandler.handle(new GetCourseOfferingDetailsQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<GetCourseOfferingStudentsResponse> getStudents(@PathVariable Long id) {
        GetCourseOfferingStudentsResponse response = getCourseOfferingStudentsQueryHandler.handle(new GetCourseOfferingStudentsQuery(id));
        return ResponseEntity.ok(response);
    }

    public record ChangeCapacityRequest(int capacity) {}

    public record ChangeProfessorRequest(String professorName) {}

    public record ChangeGuestPolicyRequest(boolean guestAllowed) {}

    public record RescheduleExamRequest(LocalDate examDate) {}

    public record SubmitGradeRequest(Long studentId, Double grade) {}
}
