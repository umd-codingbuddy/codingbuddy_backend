package com.codingbuddy.dto.academics;

import com.codingbuddy.models.courses.DifficultyLevel;
import com.codingbuddy.models.courses.Module;
import com.codingbuddy.models.courses.Page;
import com.codingbuddy.models.courses.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetail {
    private String name;
    private int courseId;
    private String description;
    private DifficultyLevel difficultyLevel;
    private Date startDate;
    private Date endDate;
    private List<Tag> tags;
    private List<ModuleDetail> modules;
    private int currentModuleId;
    private int currentPageId;
    private Long completionPercentage;
    private Boolean isOwnCourse;
}
