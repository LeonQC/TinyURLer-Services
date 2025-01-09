package com.hkx.tinyurler.service;

public interface VerificationService {
    String generateToken();
    void sendVerificationToken(String email);
    boolean verifyToken(String email, String token);
}
