package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "Endpoints para upload de vídeos")
@RequiredArgsConstructor
public class UploadController {

    private final uploadUseCase uploadUseCase;

    @PostMapping("/start")
    public ResponseEntity<?> startUpload(@RequestBody StartUploadRequest startUploadRequest) {
        StartUploadResponse response = uploadUseCase.startUpload(startUploadRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/part")
    public ResponseEntity<?> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam int partNumber,
            @RequestParam MultipartFile file
    ){
        uploadUseCase.uploadPart(UUID.fromString(uploadId), partNumber, file);
        return ResponseEntity.accepted().body("Parte recebida com sucesso!");
    }

    @PostMapping("/finish")
    public ResponseEntity<?> finishUpload(@RequestParam String uploadId) {
        uploadUseCase.completeUpload(UUID.fromString(uploadId));
        return ResponseEntity.accepted().body("Upload finalizado com sucesso!");
    }

}
