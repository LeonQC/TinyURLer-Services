package com.hkx.tinyurler.repository;

import com.hkx.tinyurler.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {

    // 根据邮箱和验证码查询
    Optional<VerificationToken> findByEmailAndToken(String email, String token);

    // 删除过期的验证码记录
    void deleteByExpiryDateBefore(LocalDateTime now);
}
