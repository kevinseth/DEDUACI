package com.DEDUACI.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DEDUACI.demo.utility.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/public")
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    creds.get("username"),
                    creds.get("password")
                )
            );

            String token = JwtUtility.generateToken(authentication);
            request.getSession().setAttribute("jwtToken", token);

            System.out.println("Session ID at login: " + request.getSession().getId());
            System.out.println("JWT stored in session: " + token);

            return ResponseEntity.ok(Map.of(
                "sessionId", request.getSession().getId(),
                "message", "Login successful",
                "token", token
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }
    }


}