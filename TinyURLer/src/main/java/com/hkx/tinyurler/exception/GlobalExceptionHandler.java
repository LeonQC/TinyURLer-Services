package com.hkx.tinyurler.exception;

import com.hkx.tinyurler.dto.response.UrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 处理 UrlNotFoundException 异常，返回 404 状态码
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<UrlResponse<Void>> handleUrlNotFoundException(UrlNotFoundException ex, WebRequest request) {
        UrlResponse<Void> response = UrlResponse.newFail(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);  // 返回 404 状态
    }

    // 捕获其他异常，返回 500 状态码
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UrlResponse<Void>> handleException(Exception ex, WebRequest request) {
        UrlResponse<Void> response = UrlResponse.newFail("An unexpected error occurred.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);  // 返回 500 状态
    }
}
