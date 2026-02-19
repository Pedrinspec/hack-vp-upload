package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;

import java.util.List;

public interface UploadPartCacheOutput {
    void save(String key, int partNumber, String eTag, Long ttlInHours);

    List<UploadPart> findByUploadIdOrderByPartNumber(String uploadId);

    void deleteAll(String key);
}
