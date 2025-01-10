package com.hkx.tinyurler.controller;


import com.hkx.tinyurler.config.RateLimited;
import com.hkx.tinyurler.dto.request.UrlRequest;
import com.hkx.tinyurler.dto.response.UrlDto;
//import com.hkx.tinyurler.dto.response.UrlResponse;
import com.hkx.tinyurler.exception.UrlNotFoundException;
import com.hkx.tinyurler.service.UrlService;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @RateLimited(apiKey = "longToShort", capacity = 50, refillTokens = 25, refillPeriod = 1)
    @PostMapping
    public ResponseEntity<UrlDto> longToShort(@RequestBody UrlRequest request) {
        UrlDto urlDto = urlService.longToShort(request.getLongUrl(),request.getTitle());

//        return UrlResponse.newSuccess(shortedUrl);
        return ResponseEntity.status(HttpStatus.OK).body(urlDto);
    }

    @RateLimited(apiKey = "redirect", capacity = 200, refillTokens = 100, refillPeriod = 1)
    @GetMapping("/{shortUrlId}")
    public ResponseEntity<Void> shortToLong(@PathVariable String shortUrlId) {
        String shortUrl = "http://localhost:8080/api/urls/" + shortUrlId;
        String longUrl = urlService.shortToLong(shortUrl);
        if (longUrl == null) {
            throw new UrlNotFoundException("Short URL not found: " + shortUrlId);
        }
        // return UrlResponse.newSuccess(longUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                             .location(URI.create(longUrl))
                             .build();
    }

    @GetMapping
    public ResponseEntity<List<UrlDto>> getAll() {
        List<UrlDto> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping("/{shortUrlId}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortUrlId) {
        String shortUrl = "http://localhost:8080/api/urls/" + shortUrlId;

        // 调用服务层删除 URL
        urlService.deleteUrl(shortUrl);

        // 返回 204 No Content 表示删除成功
        return ResponseEntity.noContent().build();
    }




}
