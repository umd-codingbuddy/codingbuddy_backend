package com.codingbuddy.controllers.course;

import com.codingbuddy.dto.course.CreateCourse;
import com.codingbuddy.dto.course.CreateModule;
import com.codingbuddy.dto.course.CreatePage;
import com.codingbuddy.dto.course.CreateTag;
import com.codingbuddy.services.course.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course")
@CrossOrigin
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/createcourse")
    public ResponseEntity<Object> createCourse(HttpServletRequest request, @RequestBody CreateCourse courseData){
        String userEmail = request.getUserPrincipal().getName();
        return courseService.createCourse(courseData, userEmail);
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

    @GetMapping("/getcourseinstructor/{courseId}")
    public ResponseEntity<Object> getCourseInstructor(@PathVariable int courseId){
        return courseService.getCourseInstructor(courseId);
    }

    @GetMapping("/getinstructorcourses")
    public ResponseEntity<Object> getInstructorCourses(HttpServletRequest request){
        String userEmail = request.getUserPrincipal().getName();
        return courseService.getInstructorCourses(userEmail);
    }

    @GetMapping("/getusercourses/{insid}")
    public ResponseEntity<Object> getUserCourses(@PathVariable int insid){
        return courseService.getUserCourses(insid);
    }

    @GetMapping("/getstudentcourses")
    public ResponseEntity<Object> getStudentCourses(HttpServletRequest request){
        String userEmail = request.getUserPrincipal().getName();
        return courseService.getStudentCourses(userEmail);
    }

    @GetMapping("/getallcourses")
    public ResponseEntity<Object> getAllCourses(){
        return courseService.getAllCourses();
    }
}
