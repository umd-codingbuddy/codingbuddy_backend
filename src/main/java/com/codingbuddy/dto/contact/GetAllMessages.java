package com.codingbuddy.dto.contact;

import com.codingbuddy.dto.course.GetAllCourses;
import com.codingbuddy.dto.user.GetUser;
import com.codingbuddy.models.user.Role;
import com.codingbuddy.models.user.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllMessages {
    private GetUser student;
    private GetAllCourses course;
    private String title;
    private String message;
    private long createdAt;
}
