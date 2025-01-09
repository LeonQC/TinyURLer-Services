package com.hkx.tinyurler.service;


import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.exception.UrlNotFoundException;
import com.hkx.tinyurler.model.Url;
import com.hkx.tinyurler.repository.UrlRepository;
import com.hkx.tinyurler.util.SecurityUtil;
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
            // 获取当前用户的唯一标识（例如邮箱）
            String currentUser = SecurityUtil.getCurrentUserEmail();
            if (currentUser == null) {
                throw new IllegalStateException("Current user email cannot be null");
            }

            // 拼接缓存键，区分不同用户
            String cacheKey = shortUrl + "-" + currentUser;

            // 更新缓存
            cacheManager.getCache("longUrlCache").put(cacheKey, longUrl);
        }
    }


    @Override
    @Cacheable(value = "shortUrlCache", key = "#longUrl + '-' + T(com.hkx.tinyurler.util.SecurityUtil).getCurrentUserEmail()")
    public UrlDto longToShort(String longUrl, String title) {
        if (longUrl == null || !longUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid long URL. Must start with http or https.");
        }

        String currentUser = SecurityUtil.getCurrentUserEmail();
        System.out.println("Current user: " + currentUser);

        // 根据当前用户和 longUrl 查询记录
        Optional<Url> optionalShortUrl = urlRepository.findByOwnerAndOriginalUrl(currentUser, longUrl);
        if (optionalShortUrl.isPresent()) {
            Url shortUrl = optionalShortUrl.get();
            updateReverseCache(shortUrl.getShortedUrl(), longUrl);
            UrlDto urlDto = new UrlDto();
            urlDto.setLongUrl(longUrl);
            urlDto.setTitle(shortUrl.getTitle());
            urlDto.setShortUrl(shortUrl.getShortedUrl());
            urlDto.setQrCode(shortUrl.getQrcode());
            return urlDto;
        } else {
            // 生成短 URL
            String shortedUrl;
            do {
                shortedUrl = urlPrefix + UrlShortener.shortenUrl();
            } while (urlRepository.findByShortedUrl(shortedUrl).isPresent());

            // 创建新的 URL 实体
            Url url = new Url();
            url.setTitle(title);
            url.setOriginalUrl(longUrl);
            url.setShortedUrl(shortedUrl);
            url.setOwner(currentUser);
            urlRepository.save(url);

            updateReverseCache(shortedUrl, longUrl);

            UrlDto urlDto = new UrlDto();
            urlDto.setTitle(title);
            urlDto.setLongUrl(longUrl);
            urlDto.setShortUrl(shortedUrl);

            return urlDto;
        }
    }


    @Override
    @Cacheable(value = "longUrlCache", key = "#shortUrl + '-' + T(com.hkx.tinyurler.util.SecurityUtil).getCurrentUserEmail()")
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
        String currentUser = SecurityUtil.getCurrentUserEmail();
        System.out.println("Current user: " + currentUser);

        List<Url> urls = urlRepository.findByOwner(currentUser); // 获取所有 URL 数据
        return urls.stream().map(url -> {
            UrlDto urlDto = new UrlDto();
            urlDto.setTitle(url.getTitle());
            urlDto.setLongUrl(url.getOriginalUrl());
            urlDto.setShortUrl(url.getShortedUrl());
            urlDto.setQrCode(url.getQrcode()); // 如果需要二维码信息
            return urlDto;
        }).collect(Collectors.toList());


    }

    @Override
    public void deleteUrl(String shortUrl) {
        String currentUser = SecurityUtil.getCurrentUserEmail();
        System.out.println("Current user: " + currentUser);

        // 查找是否存在此 URL
        Optional<Url> optionalUrl = urlRepository.findByShortedUrl(shortUrl);

        if (optionalUrl.isEmpty()) {
            throw new UrlNotFoundException("URL not found: " + shortUrl);
        }

        Url url = optionalUrl.get();

        // 检查 URL 是否属于当前用户
        if (!url.getOwner().equals(currentUser)) {
            throw new SecurityException("Unauthorized to delete this URL");
        }

        // 删除 URL
        urlRepository.delete(url);

        // 从缓存中移除（可选）
        if (cacheManager != null) {
            String cacheKey = url.getOriginalUrl() + "-" + currentUser;
            cacheManager.getCache("shortUrlCache").evict(cacheKey);
            cacheManager.getCache("longUrlCache").evict(url.getShortedUrl() + "-" + currentUser);
            if (url.getQrcode() != null) {
                cacheManager.getCache("QRCodeCache").evict(cacheKey);
            }
        }

        System.out.println("URL deleted successfully: " + shortUrl);
    }



}
