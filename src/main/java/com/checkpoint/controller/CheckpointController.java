package com.checkpoint.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckpointController {

    // Forward all non-API routes to index.html
    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }
}