package com.codingbuddy.repository.academics;

import com.codingbuddy.models.academics.StudentCourseMapping;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.user.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourseMapping, Integer> {
    StudentCourseMapping findByStudentAndCourse(Student student, Course course);
    StudentCourseMapping findByStudentAndCurrentPageId(Student student, int currentPageId);
    List<StudentCourseMapping> findByStudent(Student student);
    List<StudentCourseMapping> findByCourse(Course course);
}
