package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.S3DownloadOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadServiceImplTest {

    @Mock
    private UploadDataOutput uploadDataOutput;

    @Mock
    private S3DownloadOutput s3DownloadOutput;

    @InjectMocks
    private DownloadServiceImpl downloadService;

    @Mock
    private ResponseInputStream<GetObjectResponse> responseInputStream;

    @Test
    void shouldDownloadZipFileWhenUploadExistsAndKeyHasExtension() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "users/u1/video.mp4", StatusEnum.PROCESSED);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));
        when(s3DownloadOutput.downloadFile("users/u1/video/frames.zip")).thenReturn(responseInputStream);

        // Act
        DownloadFile result = downloadService.downloadFile(uploadId);

        // Assert
        assertThat(result.getFile()).isEqualTo(responseInputStream);
        assertThat(result.getFileName()).isEqualTo("video.zip");
        verify(s3DownloadOutput).downloadFile("users/u1/video/frames.zip");
    }

    @Test
    void shouldDownloadZipFileWhenOriginalKeyHasNoExtension() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "users/u1/video", StatusEnum.PROCESSED);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));
        when(s3DownloadOutput.downloadFile("users/u1/video/frames.zip")).thenReturn(responseInputStream);

        // Act
        DownloadFile result = downloadService.downloadFile(uploadId);

        // Assert
        assertThat(result.getFileName()).isEqualTo("video.zip");
    }

    @Test
    void shouldThrowExceptionWhenDownloadingUnknownUpload() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> downloadService.downloadFile(uploadId))
                .isInstanceOf(UploadNotExistException.class)
                .hasMessageContaining(uploadId.toString());
    }

    @Test
    void shouldReturnVideoListForUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID uploadId1 = UUID.randomUUID();
        UUID uploadId2 = UUID.randomUUID();

        Upload upload1 = buildUpload(uploadId1, "users/u1/aula1.mp4", StatusEnum.PROCESSED);
        Upload upload2 = buildUpload(uploadId2, "users/u1/aula2.mov", StatusEnum.PROCESSING_ERROR);

        when(uploadDataOutput.findByUserId(userId)).thenReturn(List.of(upload1, upload2));

        // Act
        List<VideoView> result = downloadService.getVideoList(userId);

        // Assert
        assertThat(result)
                .hasSize(2)
                .extracting(VideoView::getUploadId, VideoView::getVideoName, VideoView::getVideoStatus)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(uploadId1, "aula1.mp4", "processado"),
                        org.assertj.core.groups.Tuple.tuple(uploadId2, "aula2.mov", "erro no processamento")
                );
    }

    @Test
    void shouldReturnEmptyVideoListWhenUserHasNoVideos() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(uploadDataOutput.findByUserId(userId)).thenReturn(List.of());

        // Act
        List<VideoView> result = downloadService.getVideoList(userId);

        // Assert
        assertThat(result).isEmpty();
    }

    private Upload buildUpload(UUID uploadId, String key, StatusEnum status) {
        return Upload.builder()
                .uploadId(uploadId)
                .s3UploadId("s3-id")
                .key(key)
                .userId("user-1")
                .status(status)
                .build();
    }
}
