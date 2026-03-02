package com.checkpoint.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckpointController {
    @GetMapping("/")
    public String index() {
        return "Spring Boot is running!";
    }
}