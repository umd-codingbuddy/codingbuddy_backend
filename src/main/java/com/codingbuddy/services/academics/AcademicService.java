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
import org.python.antlr.ast.Str;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        try {
            Query query = new Query(Criteria.where("pageId").is(pageContentDto.getPageId()));
            PageContent existingPage = mongoTemplate.findOne(query, PageContent.class);

            PageContent newPageContent = PageContent.builder()
                    .pageContent(pageContentDto.getPageContent())
                    .pageId(pageContentDto.getPageId())
                    .build();

            if (existingPage == null) {
                pageContentRepository.insert(newPageContent);
                return ResponseEntity.ok().body(Map.of("status", 1));
            } else {
                PageContent updatedPage = mongoTemplate.findAndReplace(query, newPageContent);
                return ResponseEntity.ok().body(Map.of("status", 1, "message", "Page with id " + pageContentDto.getPageId() + " has been updated"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getPageContent(int pageId){
        try{
            Page page = pageRepository.findByPageId(pageId);
            return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(pageId).get("page"), "pageId", pageId, "pageType", page.getPageType(), "isHintAllowed", page.getHintAllowed()));
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Student student = studentRepository.findByUser(user);
            Page page = pageRepository.findByPageId((Integer) pageId);
            Course curCourse = page.getModule().getCourse();
            StudentCourseMapping studentCourseMapping = studentCourseRepository.findByStudentAndCourse(student, curCourse);
            if(studentCourseMapping.getCurrentPageId() != pageId){
                page = pageRepository.findByPageId((Integer) studentCourseMapping.getCurrentPageId());
                return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(page.getPageId()).get("page"), "pageId", page.getPageId(), "pageType", page.getPageType()));
            }

            HashMap<String, Object> completePage = completePage(userEmail, pageId);
            if(Integer.parseInt(String.valueOf(completePage.get("completionPercentage"))) == 100){
                return ResponseEntity.ok().body(Map.of("status", 1, "courseCompleted", true));
            }else{
                page = pageRepository.findByPageId((Integer) completePage.get("nextPageId"));
                return ResponseEntity.ok().body(Map.of("status", 1, "page", fetchPageContent(page.getPageId()).get("page"), "pageId", page.getPageId(), "pageType", page.getPageType()));
            }

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    private JSONArray fetchTestcases(int pageId){
        Map<String, Object> pageContent = fetchPageContent(pageId);
        JSONObject page = new JSONObject(pageContent.get("page"));
        return page.getJSONObject("pageContent").getJSONArray("testCases");
    }

    private List<String> runCode(String code) throws IOException, InterruptedException {
        try{
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String filePath = currentDir + File.separator + "temp.py";

            FileWriter writer = new FileWriter(filePath);
            writer.write(code);
            writer.close();

            // Open a terminal and run the Python code
            ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
            processBuilder.redirectErrorStream(true); // Redirect error stream to output stream
            Process process = processBuilder.start();
            List<String> resultString = new ArrayList<>();
            // Read and display the output of the Python process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                resultString.add(line);
            }

            // Wait for the process to finish
            process.waitFor();

            // Delete the temporary file
//            File file = new File(filePath);
//            file.delete();

            return resultString;
        }catch (Exception e){
            throw e;
        }

    }

    public ResponseEntity<Object> executeCode(ExecuteCode codeDetails) {
        try {

            // Define the Python code to execute
            String pythonCode = codeDetails.getCode();
            Map<String, Map<String, Object>> userPerformance = new HashMap<>();
            JSONArray tcs = fetchTestcases(codeDetails.getPageId());
            List<String> userOutput = new ArrayList<>();
            for(int i=0; i< tcs.length(); i++){
                JSONObject tc = tcs.getJSONObject(i);
                if(i == 0){
                    pythonCode = pythonCode + "\nprint(" + tc.getString("input") + ")";
                    userOutput = runCode(pythonCode);
                }else{
                    int lastNewLineIndex = pythonCode.lastIndexOf("\n");
                    pythonCode = pythonCode.substring(0, lastNewLineIndex + 1) + "print(" + tc.getString("input") + ")";
                    userOutput = runCode(pythonCode);
                }
                Map<String, Object> tc_output = new HashMap<>();
                tc_output.put("actualOutput", userOutput.get(userOutput.size() - 1));
                tc_output.put("expectedOutput", tc.getString("output"));
                if(userOutput.get(userOutput.size() - 1).equals(tc.getString("output"))){
                    tc_output.put("result", true);
                }else {
                    tc_output.put("result", false);
                }
                userPerformance.put(tc.getString("input"), tc_output);
            }

            return ResponseEntity.ok().body(Map.of("status", 1, "userPerformance", userPerformance));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> calculateScore(String email, CalculateScore scoreDetails){
        try {

            Page page = pageRepository.findByPageId(scoreDetails.getPageId());

            RestTemplate restTemplate = new RestTemplate();

            // Read the contents of the JSON file into a string
//            String jsonContent = new String(Files.readAllBytes(Path.of("https://codingbuddy.s3.us-east-2.amazonaws.com/lrs.json")));
            String jsonContent = restTemplate.getForObject("https://codingbuddy.s3.us-east-2.amazonaws.com/lrs.json", String.class);

            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonContent);

            jsonObject.getJSONObject("actor").getJSONObject("account").put("name", email);
            jsonObject.getJSONObject("result").put("duration", "PT" + scoreDetails.getTime() + "S");
            jsonObject.getJSONObject("result").getJSONObject("score").put("raw", scoreDetails.getScore());
            jsonObject.getJSONObject("object").put("id", "https://codingbuddy.com/course/"+page.getModule().getCourse().getCourseId());

            Map<String, Object> lrsJson = jsonObject.toMap();

            String url = "http://ec2-52-91-124-35.compute-1.amazonaws.com:8080/xapi/statements";
            String username = "d5bb52acd76f109d9d2f92540dc8450098001a37ac3d63520ff4b1649a6d4feb";
            String password = "38c14c76231832fb1d302d1851d3d2bba5f613178e46d1ba887a7d6f74693aba";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(username, password);
            headers.add("X-Experience-API-Version", "1.0.3");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(lrsJson, headers);

            // Send POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return ResponseEntity.ok().body(Map.of("status", 1, "lrsJson", responseEntity.getBody()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getStatements(int courseId){
        try{
            Course course = courseRepository.findByCourseId(courseId)
                    .orElseThrow(() -> new UsernameNotFoundException("Course not found"));
            List<StudentCourseMapping> studentCourses = studentCourseRepository.findByCourse(course);

            String username = "d5bb52acd76f109d9d2f92540dc8450098001a37ac3d63520ff4b1649a6d4feb";
            String password = "38c14c76231832fb1d302d1851d3d2bba5f613178e46d1ba887a7d6f74693aba";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.add("X-Experience-API-Version", "1.0.3");

            URI url = UriComponentsBuilder.fromHttpUrl("http://ec2-52-91-124-35.compute-1.amazonaws.com:8080")
                    .path("/xapi/statements")
                    .queryParam("activity", "https://codingbuddy.com/course/" + courseId)
                    .build()
                    .toUri();


            // Create HttpEntity with headers
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Send GET request
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            // Print the response status code and body
            System.out.println("Response status code: " + responseEntity.getStatusCode());
            System.out.println("Response body: " + responseEntity.getBody());

            JSONObject jsonObject = new JSONObject(responseEntity.getBody());

            Map<String, Object> studentPerformance = new HashMap<>();

            // Convert JSON string to JSON object

            // Print the JSON object
            System.out.println("JSON object: " + jsonObject);

            for(int i = 0; i < jsonObject.getJSONArray("statements").length(); i++) {
//                JSONObject statement = new JSONObject(statements.get(i));
                String email = (String) jsonObject.getJSONArray("statements").getJSONObject(i).getJSONObject("actor").getJSONObject("account").get("name");
                if(studentPerformance.containsKey(email)){
                    List<Integer> studentScores = (List<Integer>) studentPerformance.get(email);
                    studentScores.set(0, studentScores.get(0) + (Integer) jsonObject.getJSONArray("statements").getJSONObject(i).getJSONObject("result").getJSONObject("score").get("raw"));
                    studentScores.set(1, studentScores.get(1) + 1);
                }else{
                    List<Integer> studentScores = new ArrayList<>();
                    studentScores.add((Integer) jsonObject.getJSONArray("statements").getJSONObject(i).getJSONObject("result").getJSONObject("score").get("raw"));
                    studentScores.add(1);
                    studentPerformance.put(email, studentScores);
                }
            }
            List<Object> response = new ArrayList<>();

            for(StudentCourseMapping mapping:studentCourses){
                User user = mapping.getStudent().getUser();
                String email = user.getEmail();

                Map<String, Object> studentPerf = new HashMap<>();
                studentPerf.put("studentName", user.getFullName());
                studentPerf.put("studentId", user.getUser_id());
                studentPerf.put("completedPercentage", mapping.getCompletionPercentage());
                if(studentPerformance.get(email) == null){
                    studentPerf.put("performanceScore", -1);
                }else{
                    List<Integer> studentScores = (List<Integer>) studentPerformance.get(email);
                    studentPerf.put("performanceScore", studentScores.get(0)/studentScores.get(1));
                }
                response.add(studentPerf);

            }

            return ResponseEntity.ok().body(Map.of("status", 1, "statements", response));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }


}
