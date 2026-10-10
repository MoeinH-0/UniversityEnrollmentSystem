package com.example.universityenrollmentsystem.repository.academic;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
}
