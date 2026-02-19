package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class uploadInputPort implements uploadUseCase {

    private final UploadService uploadService;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {
        return uploadService.startUpload(request);
    }

    @Override
    public void completeUpload(UUID uploadId) {
        uploadService.completeUpload(uploadId);
    }

    @Override
    public String generatePresignedUrl(UUID uploadId, int partNumber) {
        return uploadService.generatePresignedUrl(uploadId, partNumber);
    }

    @Override
    public void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest) {
        uploadService.confirmPartUpload(uploadId, uploadPartConfirmRequest);
    }
}
