package com.fiap.vp_upload.infra.adapter.output.repository.impl;

import com.fiap.vp_upload.infra.adapter.output.repository.UploadPartCacheRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UploadPartCacheRepositoryImpl implements UploadPartCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String key, String hashKey, String hashValue, Long ttlInHours) {
        redisTemplate.opsForHash().put(key, hashKey, hashValue);
        redisTemplate.expire(key, Duration.ofHours(ttlInHours));
    }

    @Override
    public List<UploadPart> findByUploadIdOrderByPartNumber(String key) {
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries(key);

        return entries.entrySet().stream()
                .map(entry -> new UploadPart(
                        Integer.parseInt(entry.getKey().toString()),
                        entry.getValue().toString()
                ))
                .sorted(Comparator.comparingInt(UploadPart::getPartNumber))
                .toList();
    }

    @Override
    public void deleteAll(String key) {
        redisTemplate.delete(key);
    }
}
