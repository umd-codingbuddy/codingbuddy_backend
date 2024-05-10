package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.courses.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Integer> {

    Module findByModuleId(int moduleId);
    List<Module> findByCourse(Course course);
    Module findByCourseAndSequenceNumber(Course course, Long sequenceNumber);
    List<Module> findByCourseOrderBySequenceNumberAsc(Course course);
}
