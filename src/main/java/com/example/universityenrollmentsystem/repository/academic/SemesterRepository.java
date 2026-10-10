package com.example.universityenrollmentsystem.repository.academic;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
