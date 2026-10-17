package com.cao.flighttracker.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminKeyGuard {

    private final String adminKey;

    public AdminKeyGuard(@Value("${flighttracker.admin-key:}") String adminKey) {
        this.adminKey = adminKey;
    }

    public void check(String providedKey) {
        // No key configured: local development, writes stay open
        if (adminKey.isBlank()) {
            return;
        }
        if (providedKey == null || !MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AdminKeyException();
        }
    }
}