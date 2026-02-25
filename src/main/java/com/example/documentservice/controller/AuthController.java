package com.example.documentservice.controller;

import com.example.documentservice.dto.AuthRequest;
import com.example.documentservice.dto.AuthResponse;
import com.example.documentservice.service.AuthenticationService;
import com.example.documentservice.service.TokenService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    public AuthController(AuthenticationService authenticationService, TokenService tokenService) {
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        LOG.info("Register attempt for username={}", req == null ? null : req.username());
        return authenticationService.register(req).map(r -> {
            LOG.info("Register success username={}", r.username());
            return ResponseEntity.ok(r);
        }).orElseGet(() -> {
            LOG.warn("Register failed for username={}", req == null ? null : req.username());
            return ResponseEntity.badRequest().build();
        });
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        LOG.info("Login attempt for username={}", req == null ? null : req.username());
        return authenticationService.login(req).map(r -> {
            LOG.info("Login success username={}", r.username());
            return ResponseEntity.ok(r);
        }).orElseGet(() -> {
            LOG.warn("Login failed for username={}", req == null ? null : req.username());
            return ResponseEntity.status(401).build();
        });
    }
}
