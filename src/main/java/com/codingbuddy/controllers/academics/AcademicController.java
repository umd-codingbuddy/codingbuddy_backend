package com.codingbuddy.controllers.academics;

import com.codingbuddy.dto.academics.GetPageInSequence;
import com.codingbuddy.dto.academics.PageContentDto;
import com.codingbuddy.services.academics.AcademicService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academics")
public class AcademicController {

    private final AcademicService academicService;

    @PostMapping("/addstudenttocourse/{courseId}")
    public ResponseEntity<Object> addCourseToStudent(HttpServletRequest request, @PathVariable int courseId){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.addCourseToStudent(courseId, userEmail);
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

    @GetMapping("/getnextpage")
    public ResponseEntity<Object> getNextPage(HttpServletRequest request, @RequestBody GetPageInSequence getPageInSequence){
        String userEmail = request.getUserPrincipal().getName();
        return academicService.getNextPage(getPageInSequence);
    }
}
