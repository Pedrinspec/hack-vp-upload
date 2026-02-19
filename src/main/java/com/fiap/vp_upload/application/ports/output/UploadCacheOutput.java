package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;

public interface UploadCacheOutput {
    void save(String key, Upload value, Long ttlInHours);

    Upload findByKey(String key);

    void delete(String key);
}
