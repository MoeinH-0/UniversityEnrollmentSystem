package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.service.majors.addcourse.AddCourseToMajorCommand;
import com.example.universityenrollmentsystem.service.majors.addcourse.AddCourseToMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.archive.ArchiveMajorCommand;
import com.example.universityenrollmentsystem.service.majors.archive.ArchiveMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogQuery;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogQueryHandler;
import com.example.universityenrollmentsystem.service.majors.catalog.GetMajorCatalogResponse;
import com.example.universityenrollmentsystem.service.majors.create.CreateMajorCommand;
import com.example.universityenrollmentsystem.service.majors.create.CreateMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.rename.RenameMajorCommand;
import com.example.universityenrollmentsystem.service.majors.rename.RenameMajorCommandHandler;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorQuery;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorQueryHandler;
import com.example.universityenrollmentsystem.service.majors.students.GetStudentsByMajorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/majors")
public class MajorController {

    private final CreateMajorCommandHandler createMajorCommandHandler;
    private final ArchiveMajorCommandHandler archiveMajorCommandHandler;
    private final RenameMajorCommandHandler renameMajorCommandHandler;
    private final AddCourseToMajorCommandHandler addCourseToMajorCommandHandler;
    private final GetMajorCatalogQueryHandler getMajorCatalogQueryHandler;
    private final GetStudentsByMajorQueryHandler getStudentsByMajorQueryHandler;

    public MajorController(CreateMajorCommandHandler createMajorCommandHandler,
                           ArchiveMajorCommandHandler archiveMajorCommandHandler,
                           RenameMajorCommandHandler renameMajorCommandHandler,
                           AddCourseToMajorCommandHandler addCourseToMajorCommandHandler,
                           GetMajorCatalogQueryHandler getMajorCatalogQueryHandler,
                           GetStudentsByMajorQueryHandler getStudentsByMajorQueryHandler) {
        this.createMajorCommandHandler = createMajorCommandHandler;
        this.archiveMajorCommandHandler = archiveMajorCommandHandler;
        this.renameMajorCommandHandler = renameMajorCommandHandler;
        this.addCourseToMajorCommandHandler = addCourseToMajorCommandHandler;
        this.getMajorCatalogQueryHandler = getMajorCatalogQueryHandler;
        this.getStudentsByMajorQueryHandler = getStudentsByMajorQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Long> createMajor(@RequestBody CreateMajorCommand command) {
        Long majorId = createMajorCommandHandler.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/majors/" + majorId)).body(majorId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveMajor(@PathVariable Long id) {
        archiveMajorCommandHandler.handle(new ArchiveMajorCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<Void> renameMajor(@PathVariable Long id, @RequestBody RenameMajorRequest request) {
        renameMajorCommandHandler.handle(new RenameMajorCommand(id, request.name()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/courses/{courseId}")
    public ResponseEntity<Void> addCourseToMajor(@PathVariable Long id, @PathVariable Long courseId) {
        addCourseToMajorCommandHandler.handle(new AddCourseToMajorCommand(id, courseId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/catalog")
    public ResponseEntity<GetMajorCatalogResponse> getMajorCatalog(@PathVariable Long id) {
        GetMajorCatalogResponse response = getMajorCatalogQueryHandler.handle(new GetMajorCatalogQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<GetStudentsByMajorResponse> getStudentsByMajor(@PathVariable Long id) {
        GetStudentsByMajorResponse response = getStudentsByMajorQueryHandler.handle(
                new GetStudentsByMajorQuery(id));
        return ResponseEntity.ok(response);
    }

    public record RenameMajorRequest(String name) {}
}
