package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.application.ports.output.UploadCacheOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3UploadOutput s3UploadOutput;
    private final UploadDataOutput uploadDataOutput;
    private final UploadCacheOutput uploadCacheOutput;
    private final MessageOutput messageOutput;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {
        Upload upload = s3UploadOutput.startUpload(request);
        uploadDataOutput.save(upload);
        uploadCacheOutput.save(upload.getUploadId().toString(), upload, 48L);

        int totalParts = (int) Math.ceil((double) request.fileSize() / request.chunkSize());

        List<String> partsUrl = new ArrayList<>();

        for (int i = 1; i <= totalParts; i++) {
            partsUrl.add(generatePresignedUrl(upload, i));
        }

        return new StartUploadResponse(upload.getUploadId().toString(), partsUrl);
    }

    @Override
    public void completeUpload(UUID uploadId, List<UploadPart> parts) {
        Upload upload = findUpload(uploadId);
        s3UploadOutput.completeUpload(upload, parts);
        uploadCacheOutput.delete(uploadId.toString());
        upload.setStatus("COMPLETED");
        uploadDataOutput.save(upload);
        messageOutput.sendProcessMessage(new ProcessRequest(uploadId, upload.getKey()));
    }

    private String generatePresignedUrl(Upload upload, int partNumber) {
        if (partNumber < 1) {
            throw new InvalidPartNumberException();
        }

        return s3UploadOutput.generatePresignedUrl(upload, partNumber);
    }

    private Upload findUpload(UUID uploadId) {

        Upload upload = uploadCacheOutput.findByKey(uploadId.toString());
        if (upload != null) {
            System.out.println("upload resgatado do cache");
            return upload;
        }
        Upload u = uploadDataOutput.findByUploadId(uploadId)
                .orElseThrow(NullPointerException::new);
        System.out.println("upload resgatado do banco");

        uploadCacheOutput.save(uploadId.toString(), u, 48L);
        return u;
    }
}
