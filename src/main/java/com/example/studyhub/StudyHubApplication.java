package com.example.studyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StudyHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyHubApplication.class, args);
    }
}
