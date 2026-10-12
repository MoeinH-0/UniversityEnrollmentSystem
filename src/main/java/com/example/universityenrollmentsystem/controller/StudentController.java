package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.service.students.contact.UpdateStudentContactInfoCommand;
import com.example.universityenrollmentsystem.service.students.contact.UpdateStudentContactInfoCommandHandler;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsQuery;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsResponse;
import com.example.universityenrollmentsystem.service.students.major.ChangeStudentMajorCommand;
import com.example.universityenrollmentsystem.service.students.major.ChangeStudentMajorCommandHandler;
import com.example.universityenrollmentsystem.service.students.registration.RegisterStudentCommand;
import com.example.universityenrollmentsystem.service.students.registration.RegisterStudentCommandHandler;
import com.example.universityenrollmentsystem.service.students.status.ChangeStudentStatusCommand;
import com.example.universityenrollmentsystem.service.students.status.ChangeStudentStatusCommandHandler;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptQuery;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptQueryHandler;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final RegisterStudentCommandHandler registerStudentCommandHandler;
    private final ChangeStudentStatusCommandHandler changeStudentStatusCommandHandler;
    private final ChangeStudentMajorCommandHandler changeStudentMajorCommandHandler;
    private final UpdateStudentContactInfoCommandHandler updateStudentContactInfoCommandHandler;
    private final GetStudentDetailsQueryHandler getStudentDetailsQueryHandler;
    private final GetStudentTranscriptQueryHandler getStudentTranscriptQueryHandler;

    public StudentController(RegisterStudentCommandHandler registerStudentCommandHandler,
                             ChangeStudentStatusCommandHandler changeStudentStatusCommandHandler,
                             ChangeStudentMajorCommandHandler changeStudentMajorCommandHandler,
                             UpdateStudentContactInfoCommandHandler updateStudentContactInfoCommandHandler,
                             GetStudentDetailsQueryHandler getStudentDetailsQueryHandler,
                             GetStudentTranscriptQueryHandler getStudentTranscriptQueryHandler) {
        this.registerStudentCommandHandler = registerStudentCommandHandler;
        this.changeStudentStatusCommandHandler = changeStudentStatusCommandHandler;
        this.changeStudentMajorCommandHandler = changeStudentMajorCommandHandler;
        this.updateStudentContactInfoCommandHandler = updateStudentContactInfoCommandHandler;
        this.getStudentDetailsQueryHandler = getStudentDetailsQueryHandler;
        this.getStudentTranscriptQueryHandler = getStudentTranscriptQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Long> registerStudent(@RequestBody RegisterStudentCommand command) {
        Long studentId = registerStudentCommandHandler.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/students/" + studentId)).body(studentId);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestBody ChangeStatusRequest request) {
        changeStudentStatusCommandHandler.handle(new ChangeStudentStatusCommand(id, request.status()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/major")
    public ResponseEntity<Void> changeMajor(@PathVariable Long id, @RequestBody ChangeMajorRequest request) {
        changeStudentMajorCommandHandler.handle(new ChangeStudentMajorCommand(id, request.majorId()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/contact")
    public ResponseEntity<Void> updateContactInfo(@PathVariable Long id,
                                                  @RequestBody UpdateContactInfoRequest request) {
        updateStudentContactInfoCommandHandler.handle(
                new UpdateStudentContactInfoCommand(id, request.email(), request.phoneNumber()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetStudentDetailsResponse> getStudentDetails(@PathVariable Long id) {
        GetStudentDetailsResponse response = getStudentDetailsQueryHandler.handle(
                new GetStudentDetailsQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transcript")
    public ResponseEntity<GetStudentTranscriptResponse> getTranscript(@PathVariable Long id) {
        GetStudentTranscriptResponse response = getStudentTranscriptQueryHandler.handle(
                new GetStudentTranscriptQuery(id));
        return ResponseEntity.ok(response);
    }

    public record ChangeStatusRequest(StudentStatus status) {}

    public record ChangeMajorRequest(Long majorId) {}

    public record UpdateContactInfoRequest(String email, String phoneNumber) {}
}
