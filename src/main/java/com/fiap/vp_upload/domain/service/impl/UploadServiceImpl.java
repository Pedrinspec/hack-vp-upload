package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.exceptions.InvalidFileException;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final int MAX_CHUNK_SIZE_IN_MB = 10;
    private final S3UploadOutput s3UploadOutput;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {
        return s3UploadOutput.startUpload(request);
    }

    @Override
    public void uploadPart(UUID uploadId, int partNumber, MultipartFile file) {
        if (partNumber < 1) {
            throw new InvalidPartNumberException();
        }

        if(file.isEmpty()){
            throw new InvalidFileException("Arquivo está vazio");
        }

        if (file.getSize() > (MAX_CHUNK_SIZE_IN_MB * 1024 * 1024)) {
            throw new InvalidFileException(String.format("Arquivo ultrapassa o limite de %s MB", MAX_CHUNK_SIZE_IN_MB));
        }

        s3UploadOutput.uploadPart(uploadId, partNumber, file);
    }

    @Override
    public void completeUpload(UUID uploadId) {
        s3UploadOutput.completeUpload(uploadId);
    }
}
