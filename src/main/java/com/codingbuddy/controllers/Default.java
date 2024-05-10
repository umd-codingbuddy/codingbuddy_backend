package com.codingbuddy.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@CrossOrigin
public class Default {

    @GetMapping("/")
    public String getDefault(){
        return "Welcome to Coding Buddy";
    }
}
