package com.codingbuddy.services.user;

import com.codingbuddy.dto.user.UpdateUser;
import com.codingbuddy.models.user.Role;
import com.codingbuddy.repository.user.InstructorRepository;
import com.codingbuddy.repository.user.StudentRepository;
import com.codingbuddy.models.user.Instructor;
import com.codingbuddy.models.user.Student;
import com.codingbuddy.models.user.User;
import com.codingbuddy.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public ResponseEntity<Object> updateUser(String userEmail, UpdateUser userDetails){
        try{
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setAddress(userDetails.getAddress());
            user.setBio(userDetails.getBio());
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            userRepository.save(user);

            if(user.getRole() == Role.STUDENT){
                Student student = studentRepository.findByUser(user);
                student.setGithub(userDetails.getGithub());
                student.setLinkedin(userDetails.getLinkedin());
                studentRepository.save(student);
            }

            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 1, "errorMsg", e.getMessage()));
        }
    }

    public Map<String, Object> getAllUsers() {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> students = studentRepository.findAll()
                .stream()
                .map(student -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", student.getUser().getUser_id());
                    studentMap.put("name", student.getUser().getFullName());
                    studentMap.put("email", student.getUser().getEmail());
                    return studentMap;
                })
                .collect(Collectors.toList());

        response.put("students", students);
        response.put("instructors", getInstructors());

        return response;
    }

    public List<Map<String, Object>> getInstructors() {

        return instructorRepository.findAll()
                .stream()
                .map(instructor -> {
                    Map<String, Object> instructorMap = new HashMap<>();
                    instructorMap.put("id", instructor.getUser().getUser_id());
                    instructorMap.put("name", instructor.getUser().getFullName());
                    instructorMap.put("email", instructor.getUser().getEmail());
                    instructorMap.put("verified", instructor.getIsApproved());
                    return instructorMap;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Map<String, Object> response = new HashMap<>();
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("location", user.getAddress());
        response.put("bio", user.getBio());

        Student student = studentRepository.findByUser(user);
        if (student != null) {
            response.put("githubUsername", student.getGithub());
            response.put("linkedInUsername", student.getLinkedin());
        }

        Instructor instructor = instructorRepository.findByUser(user);
        if (instructor != null) {
            response.put("verified", instructor.getIsApproved());
        }

//        response.put("profileImage", "https://fastly.picsum.photos/id/826/200/300.jpg?hmac=OsVdvGZW1U_-FFoJfJrFVB-9hw0tx1H9ZyEqEaA1W10");

        return response;
    }


    public ResponseEntity<Object> deleteUser(int userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Delete associated Student or Instructor entity
            Student student = studentRepository.findByUser(user);
            if (student != null) {
                studentRepository.delete(student);
            }

            Instructor instructor = instructorRepository.findByUser(user);
            if (instructor != null) {
                instructorRepository.delete(instructor);
            }

            // Delete User entity
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "success"));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while deleting the user"));
        }
    }

    public ResponseEntity<Object> verifyInstructor(int userId){
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Instructor instructor = instructorRepository.findByUser(user);
            if (instructor == null) {
                throw new UsernameNotFoundException("User is not an instructor");
            }

            if (instructor.getIsApproved()) {
                return ResponseEntity.ok(Map.of("message", "Instructor is already verified"));
            }

            instructor.setIsApproved(true);
            instructorRepository.save(instructor);

            return ResponseEntity.ok(Map.of("message", "success"));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while verifying the instructor"));
        }
    }
}
