package com.hkx.tinyurler.service;

import com.google.zxing.WriterException;
import com.hkx.tinyurler.dto.response.UrlDto;

import java.io.IOException;
import java.util.List;

public interface QRCodeService {
    UrlDto generateQRCodeForLongUrl(String longUrl, String title) throws WriterException, IOException;
    List<UrlDto> getAllUrlsWithQRCode();
}
