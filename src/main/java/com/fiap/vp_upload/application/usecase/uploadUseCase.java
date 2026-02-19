package com.fiap.vp_upload.application.usecase;

import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface uploadUseCase {
    StartUploadResponse startUpload(StartUploadRequest request);

    void uploadPart(UUID uploadId, int partNumber, MultipartFile file);

    void completeUpload(UUID uploadId);
}
