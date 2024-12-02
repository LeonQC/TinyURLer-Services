package com.hkx.tinyurler.repository;

import com.hkx.tinyurler.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {

    Optional <Url> findByShortedUrl(String shortedUrl);
    Optional <Url> findByOriginalUrl(String originalUrl);
    List<Url> findByQrcodeIsNotNull();

}
