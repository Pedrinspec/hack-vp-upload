package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
    public void uploadPart(UUID uploadId, int partNumber, MultipartFile file) {
        uploadService.uploadPart(uploadId, partNumber, file);
    }

    @Override
    public void completeUpload(UUID uploadId) {
        uploadService.completeUpload(uploadId);
    }
}
