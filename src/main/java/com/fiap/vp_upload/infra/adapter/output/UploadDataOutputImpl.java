package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadDataOutputImpl implements UploadDataOutput {

    private final UploadRepository uploadRepository;

    @Override
    public void save(Upload upload) {
        uploadRepository.save(upload);
    }

    @Override
    public Optional<Upload> findByUploadId(UUID uploadId) {
        return uploadRepository.findByUploadId(uploadId);
    }

    @Override
    public List<Upload> findByUserId(UUID userId) {
        return uploadRepository.findByUserId(userId.toString());
    }
}
