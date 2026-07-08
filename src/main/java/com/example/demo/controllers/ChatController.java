package com.example.demo.controllers;

import com.example.demo.dto.RegisterDto;
import com.example.demo.models.User;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class ChatController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public ChatController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterDto registerDto) {
        User registerUser = authenticationService.signUp(registerDto);
        return ResponseEntity.ok(registerUser);
    }
}
