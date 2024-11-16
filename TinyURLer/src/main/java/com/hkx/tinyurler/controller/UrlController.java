package com.hkx.tinyurler.controller;


import com.hkx.tinyurler.dto.request.UrlRequest;
import com.hkx.tinyurler.dto.response.UrlResponse;
import com.hkx.tinyurler.exception.UrlNotFoundException;
import com.hkx.tinyurler.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping
    public UrlResponse<String> longToShort(@RequestBody UrlRequest request) {
        String shortedUrl = urlService.longToShort(request.getLongUrl());
        return UrlResponse.newSuccess(shortedUrl);
    }

    @GetMapping("/{shortUrlId}")
    public UrlResponse<String> shortToLong(@PathVariable String shortUrlId) {
        String longUrl = urlService.shortToLong(shortUrlId);
        if (longUrl == null) {
            throw new UrlNotFoundException("Short URL not found: " + shortUrlId);
        }
        return UrlResponse.newSuccess(longUrl);
    }

    @GetMapping("/test")
    public UrlResponse<String> test() {
        return UrlResponse.newSuccess("test response");
    }


}
