package com.codingbuddy.controllers.academics;

import com.codingbuddy.dto.academics.AddStudentToCourse;
import com.codingbuddy.dto.academics.ExecuteCode;
import com.codingbuddy.dto.academics.PageContentDto;
import com.codingbuddy.services.academics.AcademicService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academics")
@CrossOrigin
public class AcademicController {

    private final AcademicService academicService;

    @PostMapping("/addstudenttocourse")
    public ResponseEntity<Object> addCourseToStudent(HttpServletRequest request, @RequestBody AddStudentToCourse details){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.addCourseToStudent(details);
    }

    @PostMapping("/getcoursedetail/{courseId}")
    public ResponseEntity<Object> getCourseDetail(HttpServletRequest request, @PathVariable int courseId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.getCourseDetails(courseId, userEmail);
    }

    @PostMapping("/addpagecontent")
    public ResponseEntity<Object> addPageContent(HttpServletRequest request, @RequestBody PageContentDto pageContentDto){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.addPageContent(pageContentDto);
    }

    @GetMapping("/getpagecontent/{pageId}")
    public ResponseEntity<Object> getPageContent(HttpServletRequest request, @PathVariable int pageId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.getPageContent(pageId);
    }

    @GetMapping("/getnextpage/{pageId}")
    public ResponseEntity<Object> getNextPage(HttpServletRequest request, @PathVariable int pageId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.getNextPage(userEmail, pageId);
    }

    @PostMapping("/resumecourse/{courseId}")
    public ResponseEntity<Object> startCourse(HttpServletRequest request, @PathVariable int courseId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.resumeCourse(userEmail, courseId);
    }

    @PostMapping("/completepage/{pageId}")
    public ResponseEntity<Object> completePage(HttpServletRequest request, @PathVariable int pageId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.completePageReq(userEmail, pageId);
    }

//    @PostMapping("/executecode")
//    public ResponseEntity<Object> executeCode(@RequestBody ExecuteCode codeDetails){
//        return academicService.executeCode(codeDetails);
//    }
}
