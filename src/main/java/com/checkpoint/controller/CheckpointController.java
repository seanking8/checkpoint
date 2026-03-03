package com.checkpoint.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckpointController {
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}