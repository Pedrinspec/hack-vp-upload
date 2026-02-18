package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.UploadOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidChunkQuantityException;
import com.fiap.vp_upload.domain.exceptions.InvalidFileException;
import com.fiap.vp_upload.domain.model.FinishUpload;
import com.fiap.vp_upload.domain.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final UploadOutput uploadOutput;
    private static final int MAX_CHUNK_SIZE = 10;

    @Override
    public void uploadChunk(UUID uploadId, int chunkIndex, MultipartFile file) {
        if (chunkIndex < 0) {
            throw new InvalidChunkQuantityException();
        }

        if(file.isEmpty()){
            throw new InvalidFileException("Arquivo está vazio");
        }

        if(file.getSize() > MAX_CHUNK_SIZE){
            throw new InvalidFileException(String.format("Arquivo ultrapassa o limite de %s MB", MAX_CHUNK_SIZE));
        }

        uploadOutput.uploadChunk(uploadId, chunkIndex, file);
    }

    @Override
    public void finishUpload(FinishUpload finishUpload) {
        uploadOutput.finishUpload(finishUpload);
    }
}
