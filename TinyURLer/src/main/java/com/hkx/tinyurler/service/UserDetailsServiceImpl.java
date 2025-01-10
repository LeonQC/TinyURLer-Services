package com.hkx.tinyurler.service;

import com.hkx.tinyurler.model.User;
import com.hkx.tinyurler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 根据用户的 email 从数据库中查找用户
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 如果用户没有密码（OAuth2 用户），设置为一个空字符串
        String password = user.getPassword() != null ? user.getPassword() : "";

        // 返回一个 UserDetails 对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(password) // 如果是 OAuth2 登录，可以设置为空字符串
                .authorities("USER") // 设置用户的角色或权限
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActivated()) // 如果用户未激活，则禁用
                .build();
    }
}

