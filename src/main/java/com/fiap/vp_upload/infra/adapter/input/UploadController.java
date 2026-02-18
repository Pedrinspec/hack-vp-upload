package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.infra.adapter.input.dto.FinishUploadRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.fiap.vp_upload.infra.adapter.input.mapper.FinishUploadMapper.toModel;

@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "Endpoints para upload de vídeos")
@RequiredArgsConstructor
public class UploadController {

    private final uploadUseCase uploadUseCase;

    @PostMapping("/videos/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam int chunkIndex,
            @RequestParam MultipartFile file
    ){
        uploadUseCase.uploadChunk(UUID.fromString(uploadId), chunkIndex, file);
        return ResponseEntity.ok("Chunk recebido com sucesso!");
    }

    @PostMapping("/finish")
    public ResponseEntity<?> finishUpload(
            @RequestBody FinishUploadRequest request
    ){
        uploadUseCase.finishUpload(toModel(request));
        return ResponseEntity.ok("Upload finalizado com sucesso!");
    }

}
