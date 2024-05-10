package com.codingbuddy.controllers.user;

import com.codingbuddy.dto.user.UpdateUser;
import com.codingbuddy.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    @GetMapping("/getUsers")
    public Map<String, Object> getUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateUser(HttpServletRequest request, @RequestBody UpdateUser userDetails){
        String userEmail = request.getUserPrincipal().getName();
        return userService.updateUser(userEmail, userDetails);
    }

    @GetMapping("/getInstructors")
    public List<Map<String, Object>> getInstructors(){
        return userService.getInstructors();
    }

    @GetMapping("/getUser/{userId}")
    public Map<String, Object> getUser(@PathVariable int userId){
        return userService.getUser(userId);
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable int userId){
        return userService.deleteUser(userId);
    }

    @PutMapping("/verifyInstructor/{userId}")
    public ResponseEntity<Object> verifyInstructor(@PathVariable int userId){
        return userService.verifyInstructor(userId);
    }
}
