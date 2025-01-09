package com.hkx.tinyurler.service;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service

public class CrawlServiceImpl {
    public String fetchTitle(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36")
                .timeout(5000)
                .get();
        return document.title() != null ? document.title() : "";
    }

}
