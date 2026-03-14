package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.S3DownloadOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceImplTest {

    @Mock private UploadDataOutput uploadDataOutput;
    @Mock private S3DownloadOutput s3DownloadOutput;
    @InjectMocks private DownloadServiceImpl downloadService;

    @Test
    void downloadFileShouldBuildZipKeyAndName() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).key("user/u/video/movie.mp4").build();
        ResponseInputStream<GetObjectResponse> stream = mock(ResponseInputStream.class);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));
        when(s3DownloadOutput.downloadFile("user/u/video/movie/frames.zip")).thenReturn(stream);

        var result = downloadService.downloadFile(uploadId);

        assertEquals("movie.zip", result.getFileName());
        assertSame(stream, result.getFile());
    }

    @Test
    void downloadFileShouldThrowWhenUploadMissing() {
        UUID uploadId = UUID.randomUUID();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        assertThrows(UploadNotExistException.class, () -> downloadService.downloadFile(uploadId));
    }

    @Test
    void getVideoListShouldMapNameAndStatusDescription() {
        UUID userId = UUID.randomUUID();
        Upload one = Upload.builder().uploadId(UUID.randomUUID()).key("x/y/z/fileA.mp4").status(StatusEnum.PROCESSED).build();
        Upload two = Upload.builder().uploadId(UUID.randomUUID()).key("video-no-ext").status(StatusEnum.UPLOADING).build();
        when(uploadDataOutput.findByUserId(userId)).thenReturn(List.of(one, two));

        var result = downloadService.getVideoList(userId);

        assertEquals(2, result.size());
        assertEquals("fileA.mp4", result.get(0).fileName());
        assertEquals(StatusEnum.PROCESSED.getDescription(), result.get(0).status());
        assertEquals("video-no-ext", result.get(1).fileName());
    }
}
