package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    List<Tag> findByNameIn(List<String> names);
}
