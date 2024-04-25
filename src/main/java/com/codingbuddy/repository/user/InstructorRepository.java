package com.codingbuddy.repository.user;

import com.codingbuddy.models.user.Instructor;
import com.codingbuddy.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {
    Instructor findByUser(User user);
}
