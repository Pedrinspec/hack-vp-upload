package com.fiap.vp_upload.domain.service;

import com.fiap.vp_upload.domain.model.FinishUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UploadService {

    void uploadChunk(UUID uploadId, int chunkIndex, MultipartFile file);
    void finishUpload(FinishUpload finishUpload);
}
