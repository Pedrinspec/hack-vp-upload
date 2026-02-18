package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.domain.model.FinishUpload;
import com.fiap.vp_upload.domain.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class uploadInputPort implements uploadUseCase {

    private final UploadService uploadService;

    @Override
    public void uploadChunk(UUID uploadId, int chunkIndex, MultipartFile file) {
        uploadService.uploadChunk(uploadId, chunkIndex, file);
    }

    @Override
    public void finishUpload(FinishUpload finishUpload) {
        uploadService.finishUpload(finishUpload);
    }
}
