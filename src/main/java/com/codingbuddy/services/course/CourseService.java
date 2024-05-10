package com.codingbuddy.services.course;

import com.codingbuddy.dto.course.*;
import com.codingbuddy.models.academics.StudentCourseMapping;
import com.codingbuddy.models.courses.Module;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.courses.Page;
import com.codingbuddy.models.courses.Tag;
import com.codingbuddy.models.user.Instructor;
import com.codingbuddy.models.user.Role;
import com.codingbuddy.models.user.Student;
import com.codingbuddy.models.user.User;
import com.codingbuddy.repository.academics.StudentCourseRepository;
import com.codingbuddy.repository.course.CourseRepository;
import com.codingbuddy.repository.course.ModuleRepository;
import com.codingbuddy.repository.course.PageRepository;
import com.codingbuddy.repository.course.TagRepository;
import com.codingbuddy.repository.user.InstructorRepository;
import com.codingbuddy.repository.user.StudentRepository;
import com.codingbuddy.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;

    public ResponseEntity<Object> getStudentCourses(String userEmail){
        try{
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new Exception("User not found"));
            Student student = studentRepository.findByUser(user);
            List<StudentCourseMapping> studentCourseMappings = studentCourseRepository.findByStudent(student);
            List<GetStudentCourses> getStudentCourses = new ArrayList<>();

            for(StudentCourseMapping studentCourseMapping:studentCourseMappings){
                Course course = studentCourseMapping.getCourse();
                GetStudentCourses getStudentCourse = GetStudentCourses.builder()
                        .id(course.getCourseId())
                        .name(course.getName())
                        .description(course.getDescription())
                        .completionPercentage(studentCourseMapping.getCompletionPercentage())
                        .tags(course.getTags())
                        .difficultyLevel(course.getDifficultyLevel())
                        .build();

                getStudentCourses.add(getStudentCourse);
            }
            return ResponseEntity.ok().body(Map.of("status", 1, "courses", getStudentCourses));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getCourseInstructor(int courseId){
        try{
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new Exception("Course not found"));
            User user = course.getInstructor();
            Instructor instructor = instructorRepository.findByUser(user);
            GetCourseInstructorResponse instructorResponse = GetCourseInstructorResponse.builder()
                    .id(user.getUser_id())
                    .name(user.getFirstName() + " " + user.getLastName())
                    .email(user.getEmail())
                    .verified(instructor.getIsApproved())
                    .build();
            return ResponseEntity.ok().body(Map.of("status", 1, "instructor", instructorResponse));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getInstructorCourses(String userEmail){
        try{
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new Exception("User not found"));
            List<Course> courses = courseRepository.findByInstructor(user);
            List<GetInstructorCourses> instructorCourses = new ArrayList<>();
            for(Course course:courses){
                GetInstructorCourses instructorCourse = GetInstructorCourses.builder()
                        .id(course.getCourseId())
                        .name(course.getName())
                        .description(course.getDescription())
                        .tags(course.getTags())
                        .difficultyLevel(course.getDifficultyLevel())
                        .isOwnCourse(true)
                        .build();
                instructorCourses.add(instructorCourse);
            }

            return ResponseEntity.ok().body(Map.of("status", 1, "courses", instructorCourses));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getUserCourses(int insid){
        try{
            User user = userRepository.findById(insid)
                    .orElseThrow(() -> new Exception("User not found"));
            List<Course> courses = courseRepository.findByInstructor(user);
            List<GetInstructorCourses> instructorCourses = new ArrayList<>();
            for(Course course:courses){
                GetInstructorCourses instructorCourse = GetInstructorCourses.builder()
                        .id(course.getCourseId())
                        .name(course.getName())
                        .description(course.getDescription())
                        .tags(course.getTags())
                        .difficultyLevel(course.getDifficultyLevel())
                        .isOwnCourse(true)
                        .build();
                instructorCourses.add(instructorCourse);
            }

            return ResponseEntity.ok().body(Map.of("status", 1, "courses", instructorCourses));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }
    public ResponseEntity<Object> createCourse(CreateCourse courseData, String userEmail){
        try{
            List<Tag> tags = tagRepository.findByNameIn(courseData.getTags());
            List<String> dbTags = new ArrayList<>();
            for(Tag tag:tags){
                dbTags.add(tag.getName());
            }
            for(String tag:courseData.getTags()){
                if(!dbTags.contains(tag)){
                    tags.add(createTagInDb(tag));
                }
            }
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(user.getRole() != Role.INSTRUCTOR){
                throw new Exception("Course can only be added by instructor");
            }
            Course course = Course.builder()
                    .name(courseData.getName())
                    .difficultyLevel(courseData.getDifficultyLevel())
                    .description(courseData.getDescription())
                    .tags(tags)
                    .instructor(user)
                    .build();

            courseRepository.save(course);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "Course already exists"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public Tag createTagInDb(String name){
        Tag tag = Tag.builder()
                .name(name)
                .build();
        tagRepository.save(tag);
        return tag;
    }

    public ResponseEntity<Object> getAllCourses(){
        try{
            List<GetAllCourses> allCourses = new ArrayList<>();
            List<Course> courses = courseRepository.findAll();

            for(Course course:courses){
                GetAllCourses curr = GetAllCourses.builder()
                        .id(course.getCourseId())
                        .description(course.getDescription())
                        .name(course.getName())
                        .tags(course.getTags())
                        .difficultyLevel(course.getDifficultyLevel())
                        .build();

                List<Module> modules = moduleRepository.findByCourse(course);
                curr.setModules(modules.size());
                allCourses.add(curr);
            }
            return ResponseEntity.ok().body(Map.of("status", 1, "courses", allCourses));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> createTag(CreateTag tagData){
        try{
            createTagInDb(tagData.getName());
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
