package com.codingbuddy.services.academics;

import com.codingbuddy.dto.academics.*;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.courses.Module;
import com.codingbuddy.models.user.Role;
import com.codingbuddy.repository.academics.StudentCourseRepository;
import com.codingbuddy.repository.user.InstructorRepository;
import com.codingbuddy.repository.user.StudentRepository;
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

import java.util.*;

//import org.python.core.Py;
//import org.python.core.PyFunction;
//import org.python.core.PyObject;
//import org.python.util.PythonInterpreter;

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
    private final InstructorRepository instructorRepository;

    public ResponseEntity<Object> resumeCourse(String userEmail, int courseId){
        try{
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Student student = studentRepository.findByUser(user);
            Course course = courseRepository.findByCourseId(courseId)
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));
            StudentCourseMapping studentCourseMapping = studentCourseRepository.findByStudentAndCourse(student, course);
            Page page = pageRepository.findByPageId(studentCourseMapping.getCurrentPageId());

            return getPageContent(page.getPageId());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    private HashMap<String, Object> completePage(String userEmail, int pageId){
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Student student = studentRepository.findByUser(user);
            StudentCourseMapping studentCourseMapping = studentCourseRepository.findByStudentAndCurrentPageId(student, pageId);
            Page page = pageRepository.findByPageId(pageId);
            Module module = moduleRepository.findByModuleId(studentCourseMapping.getCurrentModuleId());
            Course course = studentCourseMapping.getCourse();

            List<Module> modules = moduleRepository.findByCourseOrderBySequenceNumberAsc(course);
            List<Page> pages = pageRepository.findByModuleOrderBySequenceNumberAsc(module);

            Page lastPage = pages.get(pages.size() - 1);
            if(Objects.equals(lastPage.getSequenceNumber(), page.getSequenceNumber())){
                Module lastModule = modules.get(modules.size() - 1);
                if(Objects.equals(lastModule.getSequenceNumber(), module.getSequenceNumber())){
                    studentCourseMapping.setCompletionPercentage(100L);
                    studentCourseMapping.setEndDate(new Date());
                    studentCourseRepository.save(studentCourseMapping);
                } else{
                    Module nextModule = null;
                    for (Module moduleEle : modules) {
                        if (moduleEle.getSequenceNumber() == (module.getSequenceNumber() + 1)) {
                            nextModule = moduleEle;
                            break;
                        }
                    }
                    assert nextModule != null;
                    studentCourseMapping.setCurrentModuleId(nextModule.getModuleId());
                    List<Page> nextPages = pageRepository.findByModuleOrderBySequenceNumberAsc(nextModule);
                    studentCourseMapping.setCurrentPageId(nextPages.get(0).getPageId());
                    int completionPercentage = getCompletionPercentage(modules.size(), nextPages.size(), Math.toIntExact(nextModule.getSequenceNumber()), 1);
                    studentCourseMapping.setCompletionPercentage((long) completionPercentage);
                    studentCourseRepository.save(studentCourseMapping);
                }
            }else{
                Page nextPage = null;
                for (Page pageEle : pages) {
                    if (pageEle.getSequenceNumber() == (page.getSequenceNumber() + 1)) {
                        nextPage = pageEle;
                        break;
                    }
                }
                assert nextPage != null;
                studentCourseMapping.setCurrentPageId(nextPage.getPageId());
                int completionPercentage = getCompletionPercentage(modules.size(), pages.size(), Math.toIntExact(module.getSequenceNumber()), Math.toIntExact(nextPage.getSequenceNumber()));
                studentCourseMapping.setCompletionPercentage((long) completionPercentage);
                studentCourseRepository.save(studentCourseMapping);
            }
            HashMap<String, Object> response = new HashMap<>();
            response.put("nextPageId", studentCourseMapping.getCurrentPageId());
            response.put("completionPercentage", studentCourseMapping.getCompletionPercentage());
            return response;
    }

    public ResponseEntity<Object> completePageReq(String userEmail, int pageId){
        try{
            HashMap<String, Object> response = completePage(userEmail, pageId);
            return ResponseEntity.ok().body(Map.of("status", 1, "details", response));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    private int getCompletionPercentage(int modules, int pages, int currModule, int currPage){
        int completionPercentage = 0;

        int modulePart = 100/modules;

        int modulesCompleted = currModule - 1;

        completionPercentage += modulePart*modulesCompleted;

        int pagePart = modulePart/pages;

        int pagesCompleted = currPage - 1;

        completionPercentage += pagePart*pagesCompleted;

        return completionPercentage;
    }

    public ResponseEntity<Object> addCourseToStudent(AddStudentToCourse details){
        try {
            Course course = courseRepository.findByCourseId(details.getCourseId())
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));

            User user = userRepository.findByEmail(details.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Student student = studentRepository.findByUser(user);

            List<Module> modules = moduleRepository.findByCourseOrderBySequenceNumberAsc(course);
            Module currentModule = modules.get(0);

            List<Page> pages = pageRepository.findByModuleOrderBySequenceNumberAsc(currentModule);
            Page currentPage = pages.get(0);

            StudentCourseMapping courseMapping = StudentCourseMapping.builder()
                    .course(course)
                    .student(student)
                    .completionPercentage(0L)
                    .currentModuleId(currentModule.getModuleId())
                    .currentPageId(currentPage.getPageId())
                    .startDate(new Date())
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

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Role role = user.getRole();

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

            if(role == Role.STUDENT){
                StudentCourseMapping studentCourseMapping = studentCourseRepository.findByStudentAndCourse(studentRepository.findByUser(user), course);
                Module module = moduleRepository.findByModuleId(studentCourseMapping.getCurrentModuleId());
                Page page = pageRepository.findByPageId(studentCourseMapping.getCurrentPageId());

                courseDetail.setCompletionPercentage(studentCourseMapping.getCompletionPercentage());
                courseDetail.setCurrentModuleId(module.getModuleId());
                courseDetail.setCurrentPageId(page.getPageId());
                courseDetail.setStartDate(studentCourseMapping.getStartDate());
                courseDetail.setEndDate(studentCourseMapping.getEndDate());
            }

            if(role == Role.INSTRUCTOR){
                if(instructorRepository.findByUser(course.getInstructor()) == instructorRepository.findByUser(user)){
                    courseDetail.setIsOwnCourse(true);
                }
            }


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
            Page page = pageRepository.findByPageId(pageId);
            return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(pageId).get("page"), "pageId", pageId, "pageType", page.getPageType()));
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

    public ResponseEntity<Object> getNextPage(String userEmail, int pageId){
        try{
            HashMap<String, Object> completePage = completePage(userEmail, pageId);
            if(Integer.parseInt(String.valueOf(completePage.get("completionPercentage"))) == 100){
                return ResponseEntity.ok().body(Map.of("status", 1, "courseCompleted", true));
            }else{
                Page page = pageRepository.findByPageId((Integer) completePage.get("nextPageId"));
                return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(page.getPageId()).get("page"), "pageId", page.getPageId(), "pageType", page.getPageType()));
            }

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

//    public ResponseEntity<Object> executeCode(ExecuteCode codeDetails) {
//        try {
//
//            String code = codeDetails.getCode();
//            String language = codeDetails.getLanguage();
//            String pythonCode = "def execute_python_code():\n" +
//                    "    result = 'Hello from Python!'\n" +
//                    "    return result\n" +
//                    "result = execute_python_code()";
//
//            // Initialize Python interpreter
//            PythonInterpreter interpreter = new PythonInterpreter();
//
//            // Execute Python code
//            interpreter.exec(pythonCode);
//
//            // Get the result
//            PyObject result = interpreter.get("result");
//            String resultString = "";
//
//            // Check if result is not null
//            if (result != null) {
//                // Convert result to string
//                resultString = result.toString();
//
//                // Print the result
////                System.out.println("Result of Python code execution: " + resultString);
//            } else {
//                // Print an error message if result is null
//                throw new Exception("error");
//            }
//            return ResponseEntity.ok().body(Map.of("status", 1, "executionresult", resultString));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
//        }
//    }
}
