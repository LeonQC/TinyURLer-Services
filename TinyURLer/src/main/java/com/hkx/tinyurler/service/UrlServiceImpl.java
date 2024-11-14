package com.hkx.tinyurler.service;


import com.hkx.tinyurler.model.Url;
import com.hkx.tinyurler.repository.UrlRepository;
import com.hkx.tinyurler.util.UrlShortener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;

    private final String urlPrefix = "http://tinyurler.com/";

    @Override
    @Cacheable(value = "shortUrlCache", key = "#longUrl")
    public String longToShort(String longUrl) {
        System.out.println("Fetching data from the database for longToShort: " + longUrl);
        Optional<Url> optionalShortUrl = urlRepository.findByOriginalUrl(longUrl);
        if (optionalShortUrl.isPresent()) {
            Url shortUrl = optionalShortUrl.get();
            return shortUrl.getShortedUrl();
        }
        else {
            String shortedUrl;
            do {
                shortedUrl = urlPrefix + UrlShortener.shortenUrl();
            } while (urlRepository.findByShortedUrl(shortedUrl).isPresent());

            Url url = new Url();
            url.setOriginalUrl(longUrl);
            url.setShortedUrl(shortedUrl);
            urlRepository.save(url);
            return shortedUrl;

        }


    }

    @Override
    @Cacheable(value = "longUrlCache", key = "#shortUrlId")
    public String shortToLong(String shortUrlId) {
        System.out.println("Fetching data from the database for shortToLong: " + shortUrlId);
        String shortUrl = urlPrefix + shortUrlId;
        Optional<Url> optionalLongUrl = urlRepository.findByShortedUrl(shortUrl);
        return optionalLongUrl.map(Url::getOriginalUrl).orElse(null);
    }



}
