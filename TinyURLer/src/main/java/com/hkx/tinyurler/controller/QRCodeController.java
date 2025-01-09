package com.hkx.tinyurler.controller;

import com.google.zxing.WriterException;
import com.hkx.tinyurler.config.RateLimited;
import com.hkx.tinyurler.dto.request.UrlRequest;
import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @RateLimited(apiKey = "generateQRCode", capacity = 2, refillTokens = 1, refillPeriod = 10)
    @PostMapping
    public ResponseEntity<UrlDto> generateQRCode(@RequestBody UrlRequest request) throws IOException, WriterException {
        UrlDto urlDto = qrCodeService.generateQRCodeForLongUrl(request.getLongUrl(), request.getTitle());
        return ResponseEntity.ok(urlDto);

    }

    @GetMapping
    public ResponseEntity<List<UrlDto>> getAllUrlsWithQRCode() {
        List<UrlDto> qrCodeUrls = qrCodeService.getAllUrlsWithQRCode();
        if (qrCodeUrls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(qrCodeUrls); // 返回所有二维码不为空的数据
    }


}
