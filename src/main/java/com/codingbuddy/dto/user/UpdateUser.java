package com.codingbuddy.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUser {
    private String firstName;
    private String lastName;
    private String bio;
    private String address;
    private String github;
    private String linkedin;
}
