package com.fiap.vp_upload.domain.service;

import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;

import java.util.List;
import java.util.UUID;

public interface UploadService {

    StartUploadResponse startUpload(StartUploadRequest request);

    void completeUpload(UUID uploadId, List<UploadPart> parts);
}
