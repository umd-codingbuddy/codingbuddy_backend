package com.codingbuddy.services.course;

import com.codingbuddy.models.courses.Module;
import com.codingbuddy.dto.course.CreateCourse;
import com.codingbuddy.dto.course.CreateModule;
import com.codingbuddy.dto.course.CreatePage;
import com.codingbuddy.dto.course.CreateTag;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.courses.Page;
import com.codingbuddy.models.courses.Tag;
import com.codingbuddy.repository.course.CourseRepository;
import com.codingbuddy.repository.course.ModuleRepository;
import com.codingbuddy.repository.course.PageRepository;
import com.codingbuddy.repository.course.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TagRepository tagRepository;

    private final ModuleRepository moduleRepository;

    private final PageRepository pageRepository;
    public ResponseEntity<Object> createCourse(CreateCourse courseData){

        List<Tag> tags = tagRepository.findByNameIn(courseData.getTags());
        try{
            Course course = Course.builder()
                    .name(courseData.getName())
                    .difficultyLevel(courseData.getDifficultyLevel())
                    .description(courseData.getDescription())
                    .tags(tags)
                    .build();

            courseRepository.save(course);
            return ResponseEntity.ok().body(Map.of("status", 1));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> createTag(CreateTag tagData){
        try{
            Tag tag = Tag.builder()
                    .name(tagData.getName())
                    .build();
            tagRepository.save(tag);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "Tag already exists"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> createModule(CreateModule moduleData){
        try{
            Course course = courseRepository.findById(moduleData.getCourseId())
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));

            Long sequenceNumber = (long) (moduleRepository.findByCourse(course).size() + 1);

            Module module = Module.builder()
                    .course(course)
                    .name(moduleData.getName())
                    .sequenceNumber(sequenceNumber)
                    .build();

            moduleRepository.save(module);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "Module with sequence number already exists"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> createPage(CreatePage pageData){
        try{
            Module module = moduleRepository.findById(pageData.getModuleId())
                    .orElseThrow(() -> new UsernameNotFoundException("Module not found"));

            Long sequenceNumber = (long) (pageRepository.findByModule(module).size() + 1);

            Page page = Page.builder()
                    .name(pageData.getName())
                    .module(module)
                    .difficultyLevel(pageData.getDifficultyLevel())
                    .sequenceNumber(sequenceNumber)
                    .hintAllowed(pageData.getIsHintAllowed())
                    .pageType(pageData.getPageType())
                    .build();

            pageRepository.save(page);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "Page with sequence number already exists"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }
}
