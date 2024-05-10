package com.codingbuddy.dto.course;

import com.codingbuddy.models.courses.DifficultyLevel;
import com.codingbuddy.models.courses.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetInstructorCourses {
    private int id;
    private String name;
    private DifficultyLevel difficultyLevel;
    private String description;
    private List<Tag> tags;
    private Boolean isOwnCourse;
}
