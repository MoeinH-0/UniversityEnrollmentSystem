package com.example.universityenrollmentsystem.repository.student;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRecordRepository extends JpaRepository<EnrollmentRecord, Long> {
}
