package com.fiap.vp_upload.infra.adapter.output.repository;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {
    Optional<Upload> findByUploadId(UUID uploadId);
}
