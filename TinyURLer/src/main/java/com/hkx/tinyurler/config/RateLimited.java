package com.hkx.tinyurler.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    String apiKey() default "";
    int capacity() default 10;
    int refillTokens() default 5;
    int refillPeriod() default 1;
}
