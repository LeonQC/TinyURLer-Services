package com.hkx.tinyurler.controller;


import com.hkx.tinyurler.config.RateLimited;
import com.hkx.tinyurler.dto.request.CrawlRequest;
import com.hkx.tinyurler.service.CrawlServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    @Autowired
    private CrawlServiceImpl crawlService;

    @RateLimited(apiKey = "crawl", capacity = 100, refillTokens = 50, refillPeriod = 1)
    @PostMapping("/title")
    public ResponseEntity<String> getTitle(@RequestBody CrawlRequest request) throws Exception {
        String url = request.getUrl();

        String title = crawlService.fetchTitle(url);
        return ResponseEntity.ok(title);
    }
}
