package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.uploadUseCase;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "Endpoints para upload de vídeos")
@RequiredArgsConstructor
public class UploadController {

    private final uploadUseCase uploadUseCase;

    @PostMapping("/start")
    public ResponseEntity<?> startUpload(@RequestBody StartUploadRequest startUploadRequest) {
        StartUploadResponse response = uploadUseCase.startUpload(startUploadRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("{uploadId}/complete")
    public ResponseEntity<?> finishUpload(@PathVariable String uploadId) {
        uploadUseCase.completeUpload(UUID.fromString(uploadId));
        return ResponseEntity.accepted().body("Upload finalizado com sucesso!");
    }

    @PostMapping("/{uploadId}/part/confirm")
    public ResponseEntity<?> confirmPart(@PathVariable String uploadId, @RequestBody UploadPartConfirmRequest uploadPartConfirmRequest) {
        uploadUseCase.confirmPartUpload(UUID.fromString(uploadId), uploadPartConfirmRequest);
        return ResponseEntity.accepted().build();
    }

}
