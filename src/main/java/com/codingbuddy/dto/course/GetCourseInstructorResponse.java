package com.codingbuddy.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCourseInstructorResponse {
    private int id;
    private String name;
    private String email;
    private Boolean verified;
}
