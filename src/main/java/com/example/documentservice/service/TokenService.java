package com.example.documentservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    public String generateTokenForUser(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, username);
        return token;
    }

    public Optional<String> getUsernameForToken(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(tokenToUser.get(token));
    }

    public void revokeToken(String token) {
        if (token != null) tokenToUser.remove(token);
    }
}
