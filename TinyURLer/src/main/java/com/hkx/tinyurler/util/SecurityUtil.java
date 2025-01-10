package com.hkx.tinyurler.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.userdetails.User;

public class SecurityUtil {

    // 获取当前用户的邮箱
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser) {
            // 对于 OAuth2 OIDC 登录的用户
            return ((OidcUser) principal).getEmail();
        } else if (principal instanceof DefaultOAuth2User) {
            // 对于普通 OAuth2 登录用户
            return (String) ((DefaultOAuth2User) principal).getAttributes().get("email");
        } else if (principal instanceof User) {
            // 对于使用 UserDetails 的登录用户
            return ((User) principal).getUsername(); // 假设用户名存储的是邮箱
        } else if (principal instanceof String) {
            // 对于简单的用户名（比如 JWT 中的 sub 声明是邮箱）
            return (String) principal;
        }

        throw new IllegalStateException("Unable to extract email from the current authentication.");
    }
}
