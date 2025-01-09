package com.hkx.tinyurler.repository;

import com.hkx.tinyurler.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {

    Optional <Url> findByShortedUrl(String shortedUrl);
    Optional <Url> findByOriginalUrl(String originalUrl);
    List<Url> findByQrcodeIsNotNull();

    // 查询某个用户的所有短链接数据
    List<Url> findByOwner(String owner);

    // 根据用户和短链接查找数据
    Optional<Url> findByOwnerAndShortedUrl(String owner, String shortedUrl);

    // 根据用户和长链接查找数据
    Optional<Url> findByOwnerAndOriginalUrl(String owner, String originalUrl);

    // 查找某个用户的所有已生成二维码的数据
    List<Url> findByOwnerAndQrcodeIsNotNull(String owner);

}
