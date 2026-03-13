package com.fiap.vp_upload.application.usecase;

import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;

import java.util.UUID;

public interface UploadUseCase {
    StartUploadResponse startUpload(StartUploadRequest request);

    void completeUpload(UUID uploadId);

    void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest);

    void reprocess(UUID uploadId);

    void updateStatus(StatusUpdate statusUpdate);
}
