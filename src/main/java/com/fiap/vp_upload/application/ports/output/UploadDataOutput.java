package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;

import java.util.Optional;
import java.util.UUID;

public interface UploadDataOutput {
    void save(Upload upload);

    Optional<Upload> findByUploadId(UUID uploadId);
}
