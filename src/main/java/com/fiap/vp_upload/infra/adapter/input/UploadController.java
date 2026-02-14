package com.fiap.vp_upload.infra.adapter.input;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "Endpoints para upload de vídeos")
public class UploadController {

    @PostMapping("/videos/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks
    ) throws IOException {

        Path uploadDir = Paths.get("tmp/uploads/" + uploadId);
        Files.createDirectories(uploadDir);

        Path chunkPath = uploadDir.resolve("chunk-" + chunkNumber);
        Files.write(chunkPath, file.getBytes());

        return ResponseEntity.ok().build();
    }


}
