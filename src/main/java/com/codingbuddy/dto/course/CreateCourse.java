package com.codingbuddy.dto.course;

import com.codingbuddy.models.courses.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCourse {
    private String name;
    private String description;
    private List<String> tags;
    private DifficultyLevel difficultyLevel;
}
