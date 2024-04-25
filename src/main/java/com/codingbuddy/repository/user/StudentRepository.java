package com.codingbuddy.repository.user;

import com.codingbuddy.models.user.Student;
import com.codingbuddy.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findByUser(User user);
}
