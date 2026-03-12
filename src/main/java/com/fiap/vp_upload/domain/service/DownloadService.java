package com.fiap.vp_upload.domain.service;

import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;

import java.util.List;
import java.util.UUID;

public interface DownloadService {
    DownloadFile downloadFile(UUID uploadId);

    List<VideoView> getVideoList(UUID userId);
}
