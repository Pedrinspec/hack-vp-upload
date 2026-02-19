package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.UploadPartCacheOutput;
import com.fiap.vp_upload.domain.service.UploadPartService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadPartServiceImpl implements UploadPartService {

    private final UploadPartCacheOutput uploadPartCacheOutput;

    @Override
    public List<UploadPart> findByUploadIdOrderByPartNumber(UUID uploadId) {
        return uploadPartCacheOutput.findByUploadIdOrderByPartNumber(uploadId.toString());
    }


    @Override
    public void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest) {
        uploadPartCacheOutput.save(uploadId.toString(), uploadPartConfirmRequest.partNumber(), uploadPartConfirmRequest.eTag(), 48L);
    }

    @Override
    public void clearData(UUID uploadId) {
        uploadPartCacheOutput.deleteAll(uploadId.toString());
    }
}
