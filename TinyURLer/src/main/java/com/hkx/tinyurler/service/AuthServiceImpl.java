package com.hkx.tinyurler.service;

import com.hkx.tinyurler.exception.InvalidCredentialsException;
import com.hkx.tinyurler.model.User;
import com.hkx.tinyurler.repository.UserRepository;
import com.hkx.tinyurler.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtutil;

    @Autowired
    private VerificationService verificationService;

    public void register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UnsupportedOperationException("User already exists");
        }

        // 保存用户信息，设置为未激活
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActivated(false);
        user.setRegistrationTime(LocalDateTime.now());
        userRepository.save(user);

        // 发送验证码
        verificationService.sendVerificationToken(email);
    }

    public String verifyAndRegister(String email, String token) {
        boolean isValid = verificationService.verifyToken(email, token);
        if (!isValid) {
            throw new UnsupportedOperationException("Invalid or expired verification code");
        }

        // 激活用户
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnsupportedOperationException("User not found"));

        if (user.isActivated()) {
            throw new UnsupportedOperationException("User already activated");
        }

        user.setActivated(true);
        userRepository.save(user);

        // 返回 JWT
        return jwtutil.generateToken(email);
    }

    public void resendVerificationToken(String email) {
        // 检查用户是否存在
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnsupportedOperationException("User not found"));

        if (user.isActivated()) {
            throw new UnsupportedOperationException("User is already activated");
        }

        // 发送新的验证码
        verificationService.sendVerificationToken(email);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActivated()) {
            throw new UnsupportedOperationException("User account is not activated. Please verify your email.");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 登录成功后生成 JWT
        return jwtutil.generateToken(email);
    }

    public String oauth2LoginOrRegister(String email){

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // 如果用户不存在，则注册新用户
            user = new User();
            user.setEmail(email);
            user.setPassword(null); // OAuth2 登录不需要密码
            user.setActivated(true); // Google 登录默认激活
            user.setRegistrationTime(LocalDateTime.now());
            userRepository.save(user);
        } else if (!user.isActivated()) {
            // 如果用户未激活，激活用户
            user.setActivated(true);
            userRepository.save(user);
        }

        // 返回 JWT
        return jwtutil.generateToken(email);
    }

}
