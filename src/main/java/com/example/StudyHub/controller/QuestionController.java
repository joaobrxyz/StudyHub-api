package com.example.StudyHub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {
    @GetMapping("/teste")
    public String teste(){
        return "Hello world!";
    }
}
