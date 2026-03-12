package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.S3DownloadOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;
import com.fiap.vp_upload.domain.service.DownloadService;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DownloadServiceImpl implements DownloadService {

    private final UploadDataOutput uploadDataOutput;
    private final S3DownloadOutput s3DownloadOutput;

    @Override
    public DownloadFile downloadFile(UUID uploadId) {
        Upload upload = uploadDataOutput.findByUploadId(uploadId).orElseThrow(() -> new UploadNotExistException(uploadId.toString()));
        ResponseInputStream<GetObjectResponse> file = s3DownloadOutput.downloadFile(buildZipKey(upload.getKey()));

        return DownloadFile.builder()
                .file(file)
                .fileName(buildZipFileName(upload.getKey()))
                .build();
    }

    @Override
    public List<VideoView> getVideoList(UUID userId) {
        List<Upload> uploadList = uploadDataOutput.findByUserId(userId);
        return uploadList.stream()
                .map(upload -> new VideoView(
                        upload.getUploadId(),
                        upload.getKey().substring(upload.getKey().lastIndexOf("/") + 1),
                        upload.getStatus().getDescription()))
                .toList();
    }

    private String buildZipKey(String videoKey) {

        int dotIndex = videoKey.lastIndexOf('.');
        String base = dotIndex > 0 ? videoKey.substring(0, dotIndex) : videoKey;

        return base + "/frames.zip";
    }

    private String buildZipFileName(String videoKey) {

        String fileName = videoKey.substring(videoKey.lastIndexOf("/") + 1);

        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;

        return baseName + ".zip";
    }
}
