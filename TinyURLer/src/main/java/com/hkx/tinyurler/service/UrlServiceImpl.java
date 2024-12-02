package com.hkx.tinyurler.service;


import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.model.Url;
import com.hkx.tinyurler.repository.UrlRepository;
import com.hkx.tinyurler.util.UrlShortener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheManager cacheManager;

//    @Autowired
//    private StringRedisTemplate redisTemplate;

    private final String urlPrefix = "http://localhost:8080/api/urls/";

    private void updateReverseCache(String shortUrl, String longUrl) {
        if (cacheManager != null) {
            cacheManager.getCache("longUrlCache").put(shortUrl, longUrl);
        }
    }

    @Override
    @Cacheable(value = "shortUrlCache", key = "#longUrl")

    public UrlDto longToShort(String longUrl, String title) {
        if (longUrl == null || !longUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid long URL. Must start with http or https.");
        }
        System.out.println("Fetching data from the database for longToShort: " + longUrl);
        Optional<Url> optionalShortUrl = urlRepository.findByOriginalUrl(longUrl);
        if (optionalShortUrl.isPresent()) {
            Url shortUrl = optionalShortUrl.get();
            updateReverseCache(shortUrl.getShortedUrl(), longUrl);
            UrlDto urlDto = new UrlDto();
            urlDto.setLongUrl(longUrl);
            urlDto.setTitle(shortUrl.getTitle());
            urlDto.setShortUrl(shortUrl.getShortedUrl());
            urlDto.setQrCode(shortUrl.getQrcode());
            return urlDto;
        }
        else {
            String shortedUrl;
            do {
                shortedUrl = urlPrefix + UrlShortener.shortenUrl();
            } while (urlRepository.findByShortedUrl(shortedUrl).isPresent());

            Url url = new Url();
            url.setTitle(title);
            url.setOriginalUrl(longUrl);
            url.setShortedUrl(shortedUrl);
            urlRepository.save(url);

            updateReverseCache(shortedUrl, longUrl);

            UrlDto urlDto = new UrlDto();
            urlDto.setTitle(title);
            urlDto.setLongUrl(longUrl);
            urlDto.setShortUrl(url.getShortedUrl());

            return urlDto;

        }


    }

    @Override
    @Cacheable(value = "longUrlCache", key = "#shortUrl")
    public String shortToLong(String shortUrl) {
        if (shortUrl == null || !shortUrl.startsWith(urlPrefix)) {
            throw new IllegalArgumentException("Invalid short URL. Must start with: " + urlPrefix);
        }
        System.out.println("Fetching data from the database for shortToLong: " + shortUrl);
        Optional<Url> optionalLongUrl = urlRepository.findByShortedUrl(shortUrl);
        String longUrl = optionalLongUrl.map(Url::getOriginalUrl).orElse(null);

        return longUrl;
    }

    @Override
    public List<UrlDto> getAllUrls() {
        List<Url> urls = urlRepository.findAll(); // 获取所有 URL 数据
        return urls.stream().map(url -> {
            UrlDto urlDto = new UrlDto();
            urlDto.setTitle(url.getTitle());
            urlDto.setLongUrl(url.getOriginalUrl());
            urlDto.setShortUrl(url.getShortedUrl());
            urlDto.setQrCode(url.getQrcode()); // 如果需要二维码信息
            return urlDto;
        }).collect(Collectors.toList());
    }


}
