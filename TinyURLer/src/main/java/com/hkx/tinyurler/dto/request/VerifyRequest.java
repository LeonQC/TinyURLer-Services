package com.hkx.tinyurler.dto.request;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String token;
}
