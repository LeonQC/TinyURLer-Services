package com.hkx.tinyurler.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlResponse<T> {

    private T data;
    private boolean success;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <K> UrlResponse<K> newSuccess(K data){
        UrlResponse<K> response = new UrlResponse<K>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static UrlResponse<Void> newFail(String message){
        UrlResponse<Void> response = new UrlResponse<Void>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
