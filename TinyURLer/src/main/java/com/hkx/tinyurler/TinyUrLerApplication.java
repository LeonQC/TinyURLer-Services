package com.hkx.tinyurler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TinyUrLerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TinyUrLerApplication.class, args);
    }

}
