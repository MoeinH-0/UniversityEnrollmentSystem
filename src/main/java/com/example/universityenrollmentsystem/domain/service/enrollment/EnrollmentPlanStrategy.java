package com.example.universityenrollmentsystem.domain.service.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import java.util.List;

public interface EnrollmentPlanStrategy {
    EnrollmentPlanType getPlanType();
    void validate(Student student, List<CourseOffering> courseOfferings);
}
