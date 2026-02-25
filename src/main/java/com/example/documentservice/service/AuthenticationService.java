package com.example.documentservice.service;

import com.example.documentservice.dto.AuthResponse;
import com.example.documentservice.dto.AuthRequest;
import com.example.documentservice.entity.User;
import com.example.documentservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AuthResponse> register(AuthRequest req) {
        LOG.debug("register called for {}", req == null ? null : req.username());
        if (req == null || req.username() == null || req.password() == null) return Optional.empty();
        if (userRepository.findByUsername(req.username()).isPresent()) return Optional.empty();
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(hash(req.password()));
        User saved = userRepository.save(u);
        LOG.debug("registered user id={}", saved.getId());
        return Optional.of(new AuthResponse(saved.getId(), saved.getUsername()));
    }

    public Optional<AuthResponse> login(AuthRequest req) {
        LOG.debug("login called for {}", req == null ? null : req.username());
        if (req == null || req.username() == null || req.password() == null) return Optional.empty();
        Optional<User> userOpt = userRepository.findByUsername(req.username());
        if (userOpt.isEmpty()) {
            LOG.debug("login failed - user not found: {}", req.username());
            return Optional.empty();
        }
        User u = userOpt.get();
        if (!verify(req.password(), u.getPassword())) {
            LOG.debug("login failed - password mismatch for user={}", req.username());
            return Optional.empty();
        }
        LOG.debug("login success for user={}", req.username());
        return Optional.of(new AuthResponse(u.getId(), u.getUsername()));
    }

    // expose a helper for DataInitializer to compute hashed password
    public String hashForInit(String raw) {
        return hash(raw);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verify(String raw, String hashed) {
        if (raw == null || hashed == null) return false;
        return hash(raw).equals(hashed);
    }
}
