package com.example.universityenrollmentsystem.repository.student;

import com.example.universityenrollmentsystem.domain.entity.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNumber(String studentNumber);
    List<Student> findByMajorId(Long majorId);
}
