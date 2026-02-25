package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.domain.service.UploadPartService;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class uploadInputPort implements uploadUseCase {

    private final UploadService uploadService;
    private final UploadPartService uploadPartService;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {
        return uploadService.startUpload(request);
    }

    @Override
    public void completeUpload(UUID uploadId) {
        List<UploadPart> parts = uploadPartService.findByUploadIdOrderByPartNumber(uploadId);
        uploadService.completeUpload(uploadId, parts);
        uploadPartService.clearData(uploadId);
    }

    @Override
    public void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest) {
        uploadPartService.confirmPartUpload(uploadId, uploadPartConfirmRequest);
    }
}
