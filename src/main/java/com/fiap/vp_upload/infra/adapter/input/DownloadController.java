package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.DownloadUseCase;
import com.fiap.vp_upload.domain.model.DownloadFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/download")
@Tag(name = "Download", description = "Endpoints para download de arquivos de frames")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadUseCase downloadUseCase;

    @GetMapping("/{uploadId}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String uploadId) {

        DownloadFile downloadFile =
                downloadUseCase.downloadFile(UUID.fromString(uploadId));

        StreamingResponseBody stream = outputStream -> {
            downloadFile.getFile().transferTo(outputStream);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadFile.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @GetMapping("/videos")
    public ResponseEntity<?> getVideoList(@RequestParam("user") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        return ResponseEntity.ok(downloadUseCase.getVideoList(UUID.fromString(userId)));
    }
}
