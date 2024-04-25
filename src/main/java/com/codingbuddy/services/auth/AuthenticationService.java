package com.codingbuddy.services.auth;

import com.codingbuddy.repository.user.InstructorRepository;
import com.codingbuddy.repository.user.StudentRepository;
import com.codingbuddy.dto.auth.AuthenticationRequest;
import com.codingbuddy.dto.auth.AuthenticationResponse;
import com.codingbuddy.dto.user.RegisterRequest;
import com.codingbuddy.configuration.JwtService;
import com.codingbuddy.repository.user.UserRepository;
import com.codingbuddy.models.user.Instructor;
import com.codingbuddy.models.user.Role;
import com.codingbuddy.models.user.Student;
import com.codingbuddy.models.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    private final InstructorRepository instructorRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public ResponseEntity<Object> register(RegisterRequest request){
        try {
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .address(request.getAddress())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .bio(request.getBio())
                    .build();

            User userObj = userRepository.save(user);

            if (request.getRole() == Role.STUDENT) {
                Student student = Student.builder()
                        .user(userObj)
                        .linkedin(request.getLinkedin())
                        .github(request.getGithub())
                        .build();

                studentRepository.save(student);

            } else if (request.getRole() == Role.INSTRUCTOR) {
                Instructor instructor = Instructor.builder()
                        .user(userObj)
                        .isApproved(Boolean.FALSE)
                        .build();

                instructorRepository.save(instructor);
            }
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "User already exists"));
        } catch (Exception e) {

            // Return failure response with error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> login(AuthenticationRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
            String jwtToken = jwtService.generateToken(user);
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                    .token(jwtToken)
                    .id(user.getUser_id())
                    .name(user.getFullName())
                    .role(user.getRole())
                    .build();
            return ResponseEntity.ok().body(Map.of("status", 1, "user", authenticationResponse));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "User does not exist"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", "Bad credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }
}
