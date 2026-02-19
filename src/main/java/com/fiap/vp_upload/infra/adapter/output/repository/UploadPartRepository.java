package com.fiap.vp_upload.infra.adapter.output.repository;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploadPartRepository extends JpaRepository<UploadPart, Long> {
    List<UploadPart> findByUploadIdOrderByPartNumber(UUID uploadId);
}
