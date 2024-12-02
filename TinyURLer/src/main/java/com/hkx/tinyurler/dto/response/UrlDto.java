package com.hkx.tinyurler.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlDto {
    private String title;
    private String LongUrl;
    private String ShortUrl;
    private String QrCode;

}
