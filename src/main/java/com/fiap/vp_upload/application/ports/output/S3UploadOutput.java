package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;

import java.util.List;

public interface S3UploadOutput {
    Upload startUpload(StartUploadRequest request);

    void completeUpload(Upload upload, List<UploadPart> parts);

    String generatePresignedUrl(Upload upload, int partNumber);
}
