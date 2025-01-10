package com.hkx.tinyurler.controller;

import com.hkx.tinyurler.config.RateLimited;
import com.hkx.tinyurler.dto.request.LoginRequest;
import com.hkx.tinyurler.dto.request.RegisterRequest;
import com.hkx.tinyurler.dto.request.ResendRequest;
import com.hkx.tinyurler.dto.request.VerifyRequest;
import com.hkx.tinyurler.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @RateLimited(apiKey = "register", capacity = 10, refillTokens = 5, refillPeriod = 1)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body("Verification code sent to your email.");
    }
    @RateLimited(apiKey = "verify", capacity = 100, refillTokens = 50, refillPeriod = 1)
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody VerifyRequest request) {
        String jwt = authService.verifyAndRegister(request.getEmail(), request.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(jwt);
    }

    @RateLimited(apiKey = "resend", capacity = 100, refillTokens = 50, refillPeriod = 1)
    @PostMapping("/resend")
    public ResponseEntity<String> resendVerification(@RequestBody ResendRequest request) {
        authService.resendVerificationToken(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Verification code resent to your email.");
    }

    @RateLimited(apiKey = "login", capacity = 1, refillTokens = 1, refillPeriod = 10)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String jwt = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body(jwt);
    }

    @GetMapping("/token")
    public ResponseEntity<String> getToken(HttpSession session, Authentication authentication) {
        String jwt = (String) session.getAttribute("jwt");
        String userEmail = (String) session.getAttribute("userEmail");

        // 从当前认证信息中获取用户邮箱
        String authenticatedEmail = authentication.getName();

        // 验证用户身份是否一致
        if (jwt == null || userEmail == null || !userEmail.equals(authenticatedEmail)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.ok(jwt);
    }


//    @PostMapping("/oauth2/success")
//    public ResponseEntity<String> oauth2Login(Authentication authentication) {
//        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//        String email = oidcUser.getEmail();
//
//        String jwt = authService.oauth2LoginOrRegister(email);
//        return ResponseEntity.status(HttpStatus.OK).body(jwt);
//    }

}
