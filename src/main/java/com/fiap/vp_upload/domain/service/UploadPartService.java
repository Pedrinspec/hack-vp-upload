package com.fiap.vp_upload.domain.service;

import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;

import java.util.List;
import java.util.UUID;

public interface UploadPartService {
    List<UploadPart> findByUploadIdOrderByPartNumber(UUID uploadId);

    void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest);

    void clearData(UUID uploadId);
}
