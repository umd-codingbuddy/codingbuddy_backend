package com.codingbuddy.controllers.course;

import com.codingbuddy.dto.course.CreateCourse;
import com.codingbuddy.dto.course.CreateModule;
import com.codingbuddy.dto.course.CreatePage;
import com.codingbuddy.dto.course.CreateTag;
import com.codingbuddy.services.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course")
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/createcourse")
    public ResponseEntity<Object> createCourse(@RequestBody CreateCourse courseData){
        return courseService.createCourse(courseData);
    }

    @PostMapping("/createtag")
    public ResponseEntity<Object> createTag(@RequestBody CreateTag tagData){
        return courseService.createTag(tagData);
    }

    @PostMapping("/createmodule")
    public ResponseEntity<Object> createModule(@RequestBody CreateModule moduleData){
        return courseService.createModule(moduleData);
    }

    @PostMapping("/createpage")
    public ResponseEntity<Object> createPage(@RequestBody CreatePage pageData){
        return courseService.createPage(pageData);
    }
}
