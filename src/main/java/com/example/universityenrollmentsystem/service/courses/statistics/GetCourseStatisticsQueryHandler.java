package com.example.universityenrollmentsystem.service.courses.statistics;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetCourseStatisticsQueryHandler {

    private static final int DECIMAL_SCALE = 2;

    private final CourseRepository courseRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final GetCourseStatisticsQueryValidator validator;

    public GetCourseStatisticsQueryHandler(CourseRepository courseRepository,
                                           CourseOfferingRepository courseOfferingRepository,
                                           EnrollmentRecordRepository enrollmentRecordRepository,
                                           GetCourseStatisticsQueryValidator validator) {
        this.courseRepository = courseRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetCourseStatisticsResponse handle(GetCourseStatisticsQuery query) {
        validator.validate(query);

        Course course = courseRepository.findById(query.courseId())
                .orElseThrow(() -> new CourseNotFoundException(query.courseId()));

        int totalOfferings = courseOfferingRepository.countByCourseId(query.courseId());
        List<EnrollmentRecord> courseRecords = enrollmentRecordRepository.findByCourseOfferingCourseId(query.courseId());

        GetCourseStatisticsResponse.OverallCourseStatistics overall = buildOverallStatistics(course, totalOfferings, courseRecords);
        List<GetCourseStatisticsResponse.MajorCourseStatistics> byMajor = buildMajorStatistics(courseRecords);

        return new GetCourseStatisticsResponse(overall, byMajor);
    }

    private GetCourseStatisticsResponse.OverallCourseStatistics buildOverallStatistics(
            Course course, int totalOfferings, List<EnrollmentRecord> records) {

        int passed = countByStatus(records, CourseStatus.PASSED);
        int failed = countByStatus(records, CourseStatus.FAILED);
        int inProgress = countByStatus(records, CourseStatus.IN_PROGRESS);
        double averageGrade = calculateAverageGrade(records);

        return new GetCourseStatisticsResponse.OverallCourseStatistics(
                course.getId(),
                course.getName(),
                course.getCourseCode(),
                totalOfferings,
                passed,
                failed,
                inProgress,
                averageGrade
        );
    }

    private List<GetCourseStatisticsResponse.MajorCourseStatistics> buildMajorStatistics(
            List<EnrollmentRecord> records) {

        Map<Major, List<EnrollmentRecord>> recordsByMajor = records.stream()
                .collect(Collectors.groupingBy(record -> record.getStudent().getMajor()));

        return recordsByMajor.entrySet().stream()
                .map(entry -> {
                    Major major = entry.getKey();
                    List<EnrollmentRecord> majorRecords = entry.getValue();

                    int passed = countByStatus(majorRecords, CourseStatus.PASSED);
                    int failed = countByStatus(majorRecords, CourseStatus.FAILED);
                    int inProgress = countByStatus(majorRecords, CourseStatus.IN_PROGRESS);
                    double averageGrade = calculateAverageGrade(majorRecords);

                    return new GetCourseStatisticsResponse.MajorCourseStatistics(
                            major.getId(),
                            major.getName(),
                            passed,
                            failed,
                            inProgress,
                            averageGrade
                    );
                })
                .toList();
    }

    private int countByStatus(List<EnrollmentRecord> records, CourseStatus status) {
        return (int) records.stream()
                .filter(record -> record.getStatus() == status)
                .count();
    }

    private double calculateAverageGrade(List<EnrollmentRecord> records) {
        double avg = records.stream()
                .filter(record -> record.getGrade() != null)
                .mapToDouble(EnrollmentRecord::getGrade)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg)
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
