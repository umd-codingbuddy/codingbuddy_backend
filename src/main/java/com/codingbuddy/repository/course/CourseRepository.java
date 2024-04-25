package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CourseRepository extends JpaRepository<Course, Integer> {

    Optional<Course> findByCourseId(int courseId);
}
