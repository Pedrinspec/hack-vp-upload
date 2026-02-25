package com.fiap.vp_upload.infra.adapter.output.repository.impl;

import com.fiap.vp_upload.infra.adapter.output.repository.UploadRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class UploadRedisRepositoryImpl implements UploadRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String key, String value, Long ttlInHours) {
        redisTemplate.opsForValue().set(key, value, Duration.ofHours(ttlInHours));
    }

    @Override
    public String findByKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
