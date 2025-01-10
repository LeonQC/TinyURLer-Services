package com.hkx.tinyurler.service;

import com.hkx.tinyurler.model.VerificationToken;
import com.hkx.tinyurler.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class VerificationServiceImpl implements VerificationService{

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;


    @Override
    public String generateToken() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    @Override
    public void sendVerificationToken(String email) {
        String token = generateToken();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmail(email);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(verificationToken);

        String subject = "Verification Code";
        String body = "Your verification code is: " + token + "\nThis code will expire in 5 minutes.";
        emailService.sendEmail(email, subject, body);

    }

    @Override
    public boolean verifyToken(String email, String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByEmailAndToken(email, token);

        if (optionalToken.isEmpty()) {
            return false; // 验证码不存在
        }

        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false; // 验证码过期
        }

        // 验证通过，删除验证码记录
        tokenRepository.delete(verificationToken);
        return true;
    }
}
