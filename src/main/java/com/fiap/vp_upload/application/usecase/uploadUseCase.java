package com.fiap.vp_upload.application.usecase;

import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;

import java.util.UUID;

public interface uploadUseCase {
    StartUploadResponse startUpload(StartUploadRequest request);

    void completeUpload(UUID uploadId);

    String generatePresignedUrl(UUID uploadId, int partNumber);

    void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest);
}
