package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.application.usecase.DownloadUseCase;
import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;
import com.fiap.vp_upload.domain.service.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DownloadInputPort implements DownloadUseCase {

    private final DownloadService downloadService;

    @Override
    public DownloadFile downloadFile(UUID uploadId) {
        return downloadService.downloadFile(uploadId);
    }

    @Override
    public List<VideoView> getVideoList(UUID userId) {
        return downloadService.getVideoList(userId);
    }
}
