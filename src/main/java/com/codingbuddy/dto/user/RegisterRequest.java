package com.codingbuddy.dto.user;

import com.codingbuddy.models.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
    private String bio;
    private String address;
    private String linkedin;
    private String github;

}
