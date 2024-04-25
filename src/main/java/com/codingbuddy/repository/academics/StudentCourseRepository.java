package com.codingbuddy.repository.academics;

import com.codingbuddy.models.academics.StudentCourseMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseRepository extends JpaRepository<StudentCourseMapping, Integer> {

}
