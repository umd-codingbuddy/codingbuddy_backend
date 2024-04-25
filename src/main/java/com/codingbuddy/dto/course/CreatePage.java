package com.codingbuddy.dto.course;

import com.codingbuddy.models.courses.DifficultyLevel;
import com.codingbuddy.models.courses.PageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePage {
    private String name;
    private int moduleId;
    private Boolean isHintAllowed;
    private PageType pageType;
    private DifficultyLevel difficultyLevel;
}
