package com.example.universityenrollmentsystem.service.students.transcript;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetStudentTranscriptQueryHandler {

    private static final int DECIMAL_SCALE = 2;

    private final StudentRepository studentRepository;
    private final GetStudentTranscriptQueryValidator validator;

    public GetStudentTranscriptQueryHandler(StudentRepository studentRepository,
                                            GetStudentTranscriptQueryValidator validator) {
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetStudentTranscriptResponse handle(GetStudentTranscriptQuery query) {
        validator.validate(query);

        Student student = studentRepository.findById(query.studentId())
                .orElseThrow(() -> new StudentNotFoundException(query.studentId()));

        List<EnrollmentRecord> enrollments = student.getEnrollments();

        double overallGpa = calculateGpa(enrollments);
        int totalPassedCredits = enrollments.stream()
                .filter(record -> record.getStatus() == CourseStatus.PASSED)
                .mapToInt(record -> record.getCourseOffering().getCourse().getCredits())
                .sum();

        Map<Semester, List<EnrollmentRecord>> recordsBySemester = enrollments.stream()
                .collect(Collectors.groupingBy(record -> record.getCourseOffering().getSemester()));

        List<GetStudentTranscriptResponse.SemesterTranscriptDto> semesterDtos = recordsBySemester.entrySet().stream()
                .map(entry -> {
                    Semester semester = entry.getKey();
                    List<EnrollmentRecord> semesterRecords = entry.getValue();

                    double semesterGpa = calculateGpa(semesterRecords);
                    int semesterCredits = semesterRecords.stream()
                            .filter(record -> record.getStatus() == CourseStatus.PASSED)
                            .mapToInt(record -> record.getCourseOffering().getCourse().getCredits())
                            .sum();

                    List<GetStudentTranscriptResponse.CourseGradeDto> courseDtos = semesterRecords.stream()
                            .map(record -> new GetStudentTranscriptResponse.CourseGradeDto(
                                    record.getCourseOffering().getCourse().getId(),
                                    record.getCourseOffering().getCourse().getName(),
                                    record.getCourseOffering().getCourse().getCourseCode(),
                                    record.getCourseOffering().getCourse().getCredits(),
                                    record.getGrade(),
                                    record.getStatus()
                            ))
                            .toList();

                    return new GetStudentTranscriptResponse.SemesterTranscriptDto(
                            semester.getId(),
                            semester.getTitle(),
                            semesterGpa,
                            semesterCredits,
                            courseDtos
                    );
                })
                .toList();

        return new GetStudentTranscriptResponse(
                student.getId(),
                student.getFullName(),
                student.getStudentNumber(),
                student.getMajor().getName(),
                overallGpa,
                totalPassedCredits,
                semesterDtos
        );
    }

    private double calculateGpa(List<EnrollmentRecord> records) {
        double totalWeightedGrade = 0.0;
        int totalCredits = 0;

        for (EnrollmentRecord record : records) {
            if (record.getGrade() != null) {
                int credits = record.getCourseOffering().getCourse().getCredits();
                totalWeightedGrade += record.getGrade() * credits;
                totalCredits += credits;
            }
        }

        if (totalCredits == 0)
            return 0.0;

        double rawGpa = totalWeightedGrade / totalCredits;

        return BigDecimal.valueOf(rawGpa)
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
