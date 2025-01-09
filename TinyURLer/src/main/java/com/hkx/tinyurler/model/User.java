package com.hkx.tinyurler.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private boolean isActivated;      // 是否激活
    private LocalDateTime registrationTime; // 注册时间
}
