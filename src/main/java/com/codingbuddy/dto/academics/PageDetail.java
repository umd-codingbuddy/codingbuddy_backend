package com.codingbuddy.dto.academics;

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
public class PageDetail {
    private String name;
    private PageType pageType;
    private Long sequenceNumber;
    private DifficultyLevel difficultyLevel;
    private Boolean hintAllowed;
    private int pageId;
}
