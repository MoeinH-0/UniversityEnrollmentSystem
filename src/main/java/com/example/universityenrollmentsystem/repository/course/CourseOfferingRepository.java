package com.example.universityenrollmentsystem.repository.course;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {
    List<CourseOffering> findBySemesterId(Long semesterId);
}
