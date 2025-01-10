package com.hkx.tinyurler.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
}
