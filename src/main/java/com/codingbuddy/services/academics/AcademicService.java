package com.codingbuddy.services.academics;

import com.codingbuddy.dto.academics.*;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.courses.Module;
import com.codingbuddy.repository.academics.StudentCourseRepository;
import com.codingbuddy.repository.user.StudentRepository;
import com.codingbuddy.dto.academics.*;
import com.codingbuddy.models.academics.StudentCourseMapping;
import com.codingbuddy.models.courses.Page;
import com.codingbuddy.models.courses.PageContent;
import com.codingbuddy.models.user.Student;
import com.codingbuddy.models.user.User;
import com.codingbuddy.repository.course.CourseRepository;
import com.codingbuddy.repository.course.ModuleRepository;
import com.codingbuddy.repository.course.PageContentRepository;
import com.codingbuddy.repository.course.PageRepository;
import com.codingbuddy.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final PageRepository pageRepository;
    private final PageContentRepository pageContentRepository;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<Object> addCourseToStudent(int courseId, String studentEmail){
        try {
            Course course = courseRepository.findByCourseId(courseId)
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));

            User user = userRepository.findByEmail(studentEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Student student = studentRepository.findByUser(user);

            StudentCourseMapping courseMapping = StudentCourseMapping.builder()
                    .course(course)
                    .student(student)
                    .completionPercentage(0L)
                    .currentModuleId(0L)
                    .build();

            studentCourseRepository.save(courseMapping);
            return ResponseEntity.ok().body(Map.of("status", 1));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getCourseDetails(int courseId, String userEmail){
        try{
            Course course = courseRepository.findByCourseId(courseId)
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));

            List<Module> modules = moduleRepository.findByCourse(course);

            List<ModuleDetail> moduleDetails = new ArrayList<>();

            int i = 0;

            while(i < modules.size()){
                Module module = modules.get(i);
                List<Page> pages = pageRepository.findByModule(module);
                List<PageDetail> pageDetails = new ArrayList<>();
                int j = 0;

                while (j < pages.size()){
                    Page page = pages.get(j);
                    PageDetail pageDetail = PageDetail.builder()
                            .name(page.getName())
                            .sequenceNumber(page.getSequenceNumber())
                            .difficultyLevel(page.getDifficultyLevel())
                            .hintAllowed(page.getHintAllowed())
                            .pageType(page.getPageType())
                            .pageId(page.getPageId())
                            .build();

                    pageDetails.add(pageDetail);
                    j++;
                }

                ModuleDetail moduleDetail = ModuleDetail.builder()
                        .name(module.getName())
                        .sequenceNumber(module.getSequenceNumber())
                        .moduleId(module.getModuleId())
                        .pages(pageDetails)
                        .build();

                moduleDetails.add(moduleDetail);
                i++;
            }

            CourseDetail courseDetail = CourseDetail.builder()
                    .courseId(course.getCourseId())
                    .description(course.getDescription())
                    .name(course.getName())
                    .difficultyLevel(course.getDifficultyLevel())
                    .modules(moduleDetails)
                    .tags(course.getTags())
                    .build();


            return ResponseEntity.ok().body(Map.of("status", 1, "courseDetail", courseDetail));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> addPageContent(PageContentDto pageContentDto){
        try{
            PageContent pageContent = PageContent.builder()
                    .pageContent(pageContentDto.getPageContent())
                    .pageId(pageContentDto.getPageId())
                    .build();

            pageContentRepository.insert(pageContent);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getPageContent(int pageId){
        try{
            return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(pageId).get("page")));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    private Map<String, Object> fetchPageContent(int pageId){
        Query query = new Query();
        query.addCriteria(Criteria.where("pageId").is(pageId));
        List<PageContent> pageContent = mongoTemplate.find(query, PageContent.class);
        if (pageContent.size() > 1) {
            throw new IllegalStateException("Found many pages with id " + pageId);
        } else if (pageContent.isEmpty()) {
            throw new IllegalStateException("No page found with id " + pageId);
        }
        return Map.of("page", pageContent.get(0));
    }

    public ResponseEntity<Object> getNextPage(GetPageInSequence getPageInSequence){
        try{
            List<Page> pages = pageRepository.findByModuleIdSorted(getPageInSequence.getModuleId());
            if(pages.size() < getPageInSequence.getSequenceNumber()){
                throw new Exception("Page with sequence number " + getPageInSequence.getSequenceNumber() + " doesn't exist");
            }
            Page page = pages.get(getPageInSequence.getSequenceNumber() + 1);

            return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(page.getPageId()).get("page")));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }
}
