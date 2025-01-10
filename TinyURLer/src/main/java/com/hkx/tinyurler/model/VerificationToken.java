package com.hkx.tinyurler.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@Document(collection = "verification_tokens")
public class VerificationToken {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;             // 邮箱字段，唯一索引

    private String token;             // 验证码

    private LocalDateTime expiryDate; // 设置过期时间的 TTL 索引
}
