package com.hkx.tinyurler.service;

import com.hkx.tinyurler.dto.response.UrlDto;
import com.hkx.tinyurler.model.Url;

import java.util.List;

public interface UrlService {
    UrlDto longToShort(String longUrl, String title);
    String shortToLong(String shortUrlId);
    List<UrlDto> getAllUrls();

}
