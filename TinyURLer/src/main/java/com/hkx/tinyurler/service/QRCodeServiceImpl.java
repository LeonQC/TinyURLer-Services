package com.hkx.tinyurler.service;

import com.google.zxing.WriterException;
import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.model.Url;
import com.hkx.tinyurler.repository.UrlRepository;
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
            cacheManager.getCache("shortUrlCache").put(longUrl, shortUrlDto);
        }
    }

    private void updateReverseCache(String shortUrl, String longUrl) {
        if (cacheManager != null) {
            cacheManager.getCache("longUrlCache").put(shortUrl, longUrl);
        }
    }



    @Cacheable(value = "QRCodeCache", key = "#longUrl")
    @Override
    public UrlDto generateQRCodeForLongUrl(String longUrl, String title) throws WriterException, IOException {

        if (longUrl == null || !longUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid long URL. Must start with http or https.");
        }

        System.out.println("Fetching data from the database for QRcode: " + longUrl);

        // 查找已生成的记录
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(longUrl);
        if (existingUrl.isPresent()) {
            if (existingUrl.get().getQrcode() != null) {
                // 如果已有记录，且已生成二维码，直接返回
                UrlDto existingDto = new UrlDto();
                existingDto.setLongUrl(longUrl);
                existingDto.setTitle(title);
                existingDto.setShortUrl(existingUrl.get().getShortedUrl());
                existingDto.setQrCode(existingUrl.get().getQrcode());
                updateReverseCache(existingDto.getShortUrl(), longUrl);
                return existingDto;
            }
            // 如果已有记录但未生成二维码，则更新记录
            Url url = existingUrl.get();
            String qrCodeBase64 = QRCodeUtil.generateQRCode(url.getOriginalUrl(), 300, 300);
            url.setQrcode(qrCodeBase64);
            urlRepository.save(url);

            UrlDto dto = new UrlDto();
            dto.setLongUrl(longUrl);
            dto.setTitle(title);
            dto.setShortUrl(url.getShortedUrl());
            dto.setQrCode(qrCodeBase64);
            updateShortUrlCache(longUrl, dto);
            updateReverseCache(dto.getShortUrl(), longUrl);
            return dto;
        }

        // 生成短链接
        String shortedUrl;
        do {
            shortedUrl = urlPrefix + UrlShortener.shortenUrl();
        } while (urlRepository.findByShortedUrl(shortedUrl).isPresent());

        // 生成二维码
        String qrCodeBase64 = QRCodeUtil.generateQRCode(longUrl, 300, 300);

        // 保存到数据库
        Url url = new Url();
        url.setTitle(title);
        url.setOriginalUrl(longUrl);
        url.setShortedUrl(shortedUrl);
        url.setQrcode(qrCodeBase64);
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
        System.out.println("Fetching all URLs with QR codes.");
        List<Url> urls = urlRepository.findByQrcodeIsNotNull();
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
