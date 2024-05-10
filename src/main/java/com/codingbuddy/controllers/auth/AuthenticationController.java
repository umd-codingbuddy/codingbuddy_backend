package com.codingbuddy.controllers.auth;

import com.codingbuddy.dto.auth.AuthenticationRequest;
import com.codingbuddy.services.auth.AuthenticationService;
import com.codingbuddy.dto.user.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody RegisterRequest request
    ){
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> register(
            @RequestBody AuthenticationRequest request
    ){
        return authenticationService.login(request);
    }
}
