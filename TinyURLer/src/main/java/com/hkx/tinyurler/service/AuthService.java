package com.hkx.tinyurler.service;



public interface AuthService {

    void register(String email, String password);

    String verifyAndRegister(String email, String token);

    String login(String email, String password);

    void resendVerificationToken(String email);

    String oauth2LoginOrRegister(String email);
}
