package com.fiap.vp_upload.infra.adapter.output.repository;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;

import java.util.List;

public interface UploadPartCacheRepository {
    void save(String key, String hashKey, String hashValue, Long ttlInHours);

    List<UploadPart> findByUploadIdOrderByPartNumber(String uploadId);

    void deleteAll(String key);
}
