package com.hkx.tinyurler.config;


import com.hkx.tinyurler.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimiterAspect {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private Bucket createBucket(RateLimited rateLimited) {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(rateLimited.capacity(),
                                Refill.greedy(rateLimited.refillTokens(), Duration.ofSeconds(rateLimited.refillPeriod()))
                        )
                )
                .build();
    }


    @Around("@annotation(rateLimited)")
    public Object limitRate(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String apiKey = rateLimited.apiKey();
        if (apiKey.isEmpty()){
            apiKey = joinPoint.getSignature().toShortString();

        }

        Bucket bucket = bucketCache.computeIfAbsent(apiKey, key -> createBucket(rateLimited));

        if (bucket.tryConsume(1)){
            return joinPoint.proceed();
        }
        else {
            throw new RateLimitExceededException("Too many requests. Try again later.");

        }
    }
}
