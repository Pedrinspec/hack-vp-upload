package com.fiap.vp_upload.application.usecase;

import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;

import java.util.List;
import java.util.UUID;

public interface DownloadUseCase {

    DownloadFile downloadFile(UUID uploadId);

    List<VideoView> getVideoList(UUID userId);
}
