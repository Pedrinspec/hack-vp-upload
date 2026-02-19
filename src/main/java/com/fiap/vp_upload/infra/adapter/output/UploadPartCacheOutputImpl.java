package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.UploadPartCacheOutput;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadPartCacheRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UploadPartCacheOutputImpl implements UploadPartCacheOutput {

    private static final String UPLOAD_PART_BASE_KEY = "vp-upload";

    private final UploadPartCacheRepository uploadPartCacheRepository;

    @Override
    public void save(String uploadId, int partNumber, String eTag, Long ttlInHours) {
        uploadPartCacheRepository.save(String.format("%s:%s:parts", UPLOAD_PART_BASE_KEY, uploadId), String.valueOf(partNumber), eTag, ttlInHours);
    }

    @Override
    public List<UploadPart> findByUploadIdOrderByPartNumber(String uploadId) {
        return uploadPartCacheRepository.findByUploadIdOrderByPartNumber(String.format("%s:%s:parts", UPLOAD_PART_BASE_KEY, uploadId));
    }

    @Override
    public void deleteAll(String uploadId) {
        uploadPartCacheRepository.deleteAll(String.format("%s:%s:parts", UPLOAD_PART_BASE_KEY, uploadId));
    }
}
