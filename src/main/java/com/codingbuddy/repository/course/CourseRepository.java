package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CourseRepository extends JpaRepository<Course, Integer> {

    Optional<Course> findByCourseId(int courseId);

    List<Course> findByInstructor(User instructor);
}
