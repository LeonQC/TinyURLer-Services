package com.hkx.tinyurler.service;

import com.google.zxing.WriterException;
import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.model.Url;
import com.hkx.tinyurler.repository.UrlRepository;
import com.hkx.tinyurler.util.SecurityUtil;
import com.hkx.tinyurler.util.UrlShortener;
import com.hkx.tinyurler.util.QRCodeUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QRCodeServiceImpl implements QRCodeService{

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheManager cacheManager;

    private final String urlPrefix = "http://localhost:8080/api/urls/";

    private void updateShortUrlCache(String longUrl, UrlDto shortUrlDto) {
        if (cacheManager != null) {
            String currentUser = SecurityUtil.getCurrentUserEmail(); // 获取当前用户邮箱
            String cacheKey = longUrl + "-" + currentUser; // 生成唯一缓存键
            cacheManager.getCache("shortUrlCache").put(cacheKey, shortUrlDto); // 更新缓存
        }
    }

    // 更新 longUrlCache，缓存键中包含用户信息
    private void updateReverseCache(String shortUrl, String longUrl) {
        if (cacheManager != null) {
            String currentUser = SecurityUtil.getCurrentUserEmail(); // 获取当前用户邮箱
            String cacheKey = shortUrl + "-" + currentUser; // 生成唯一缓存键
            cacheManager.getCache("longUrlCache").put(cacheKey, longUrl); // 更新缓存
        }
    }



    @Cacheable(value = "QRCodeCache", key = "#longUrl + '-' + T(com.hkx.tinyurler.util.SecurityUtil).getCurrentUserEmail()")
    @Override
    public UrlDto generateQRCodeForLongUrl(String longUrl, String title) throws WriterException, IOException {

        if (longUrl == null || !longUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid long URL. Must start with http or https.");
        }

        // 获取当前用户
        String currentUser = SecurityUtil.getCurrentUserEmail();
        System.out.println("Current user: " + currentUser);

        System.out.println("Fetching data from the database for QR code: " + longUrl);

        // 查找已存在记录
        Optional<Url> existingUrl = urlRepository.findByOwnerAndOriginalUrl(currentUser, longUrl);

        if (existingUrl.isPresent()) {
            Url url = existingUrl.get();

            // 如果二维码已存在，直接返回
            if (url.getQrcode() != null) {
                UrlDto existingDto = new UrlDto();
                existingDto.setLongUrl(longUrl);
                existingDto.setTitle(url.getTitle());
                existingDto.setShortUrl(url.getShortedUrl());
                existingDto.setQrCode(url.getQrcode());
                updateReverseCache(existingDto.getShortUrl(), longUrl);
                return existingDto;
            }

            // 如果记录存在，但未生成二维码，则生成并更新二维码
            String qrCodeBase64 = QRCodeUtil.generateQRCode(url.getOriginalUrl(), 300, 300);
            url.setQrcode(qrCodeBase64); // 更新二维码字段
            urlRepository.save(url); // 保存更新后的记录

            UrlDto dto = new UrlDto();
            dto.setLongUrl(longUrl);
            dto.setTitle(title);
            dto.setShortUrl(url.getShortedUrl());
            dto.setQrCode(qrCodeBase64);
            updateShortUrlCache(longUrl, dto);
            updateReverseCache(dto.getShortUrl(), longUrl);
            return dto;
        }

        // 如果记录不存在，则生成短链接和二维码，并保存新记录
        String shortedUrl;
        do {
            shortedUrl = urlPrefix + UrlShortener.shortenUrl();
        } while (urlRepository.findByShortedUrl(shortedUrl).isPresent());

        // 生成二维码
        String qrCodeBase64 = QRCodeUtil.generateQRCode(longUrl, 300, 300);

        // 保存新记录到数据库
        Url url = new Url();
        url.setTitle(title);
        url.setOriginalUrl(longUrl);
        url.setShortedUrl(shortedUrl);
        url.setQrcode(qrCodeBase64);
        url.setOwner(currentUser);
        urlRepository.save(url);

        // 构造并返回 DTO
        UrlDto dto = new UrlDto();
        dto.setTitle(title);
        dto.setLongUrl(longUrl);
        dto.setShortUrl(shortedUrl);
        dto.setQrCode(qrCodeBase64);
        updateShortUrlCache(longUrl, dto);
        updateReverseCache(dto.getShortUrl(), longUrl);

        return dto;
    }



    @Override
    public List<UrlDto> getAllUrlsWithQRCode() {
        String currentUser = SecurityUtil.getCurrentUserEmail();
        System.out.println("Current user: " + currentUser);

        System.out.println("Fetching all URLs with QR codes.");
        List<Url> urls = urlRepository.findByOwnerAndQrcodeIsNotNull(currentUser);
        System.out.println("Number of URLs found: " + urls.size());
        return urls.stream().map(url -> {
            UrlDto dto = new UrlDto();
            dto.setTitle(url.getTitle());
            dto.setLongUrl(url.getOriginalUrl());
            dto.setShortUrl(url.getShortedUrl());
            dto.setQrCode(url.getQrcode());
            return dto;
        }).collect(Collectors.toList());
    }
}
