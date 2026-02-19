package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3UploadOutput s3UploadOutput;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {
        return s3UploadOutput.startUpload(request);
    }

    @Override
    public void completeUpload(UUID uploadId) {
        s3UploadOutput.completeUpload(uploadId);
    }

    @Override
    public String generatePresignedUrl(UUID uploadId, int partNumber) {
        if (partNumber < 1) {
            throw new InvalidPartNumberException();
        }
        return s3UploadOutput.generatePresignedUrl(uploadId, partNumber);
    }

    @Override
    public void confirmPartUpload(UUID uploadId, UploadPartConfirmRequest uploadPartConfirmRequest) {
        s3UploadOutput.confirmPartUpload(uploadId, uploadPartConfirmRequest);
    }
}
