package com.example.universityenrollmentsystem.repository.student;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRecordRepository extends JpaRepository<EnrollmentRecord, Long> {
    Optional<EnrollmentRecord> findByStudentIdAndCourseOfferingId(Long studentId, Long courseOfferingId);
    List<EnrollmentRecord> findByCourseOfferingId(Long courseOfferingId);
}
