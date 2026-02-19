package com.fiap.vp_upload.infra.adapter.output.repository;

public interface UploadRedisRepository {

    void save(String key, String value, Long ttlInHours);

    String findByKey(String key);

    void delete(String key);
}
