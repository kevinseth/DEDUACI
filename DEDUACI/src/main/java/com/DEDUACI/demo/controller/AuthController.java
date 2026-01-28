package com.DEDUACI.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping({"/", "/index"})
    public String showIndexPage() {
        return "index"; // your homepage / login form template
    }

    // ✅ Add this mapping for Spring Security login redirect
    @GetMapping("/login")
    public String showLoginPage() {
        return "index"; // same index.html, or a dedicated login.html if you want
    }
}
