package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.UploadCacheOutput;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadRedisRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UploadCacheOutputImpl implements UploadCacheOutput {

    private static final String UPLOAD_BASE_KEY = "vp-upload";

    private final Gson gson;
    private final UploadRedisRepository uploadRedisRepository;

    @Override
    public void save(String key, Upload value, Long ttlInHours) {
        uploadRedisRepository.save(String.format("%s:%s", UPLOAD_BASE_KEY, key), gson.toJson(value), ttlInHours);
    }

    @Override
    public Upload findByKey(String key) {
        return gson.fromJson(uploadRedisRepository.findByKey(String.format("%s:%s", UPLOAD_BASE_KEY, key)), Upload.class);
    }

    @Override
    public void delete(String key) {
        uploadRedisRepository.delete(String.format("%s:%s", UPLOAD_BASE_KEY, key));
    }
}
