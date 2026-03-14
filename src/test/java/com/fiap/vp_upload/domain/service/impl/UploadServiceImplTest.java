package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.application.ports.output.UploadCacheOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock
    private S3UploadOutput s3UploadOutput;

    @Mock
    private UploadDataOutput uploadDataOutput;

    @Mock
    private UploadCacheOutput uploadCacheOutput;

    @Mock
    private MessageOutput messageOutput;

    @InjectMocks
    private UploadServiceImpl uploadService;

    @Test
    void shouldStartUploadSuccessfullyAndGenerateAllPresignedUrls() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        StartUploadRequest request = new StartUploadRequest("user-1", "video.mp4", 10L, 3L);
        Upload upload = buildUpload(uploadId, "users/user-1/video.mp4", StatusEnum.PROCESSING);

        when(s3UploadOutput.startUpload(request)).thenReturn(upload);
        when(s3UploadOutput.generatePresignedUrl(upload, 1)).thenReturn("url-1");
        when(s3UploadOutput.generatePresignedUrl(upload, 2)).thenReturn("url-2");
        when(s3UploadOutput.generatePresignedUrl(upload, 3)).thenReturn("url-3");
        when(s3UploadOutput.generatePresignedUrl(upload, 4)).thenReturn("url-4");

        // Act
        StartUploadResponse response = uploadService.startUpload(request);

        // Assert
        assertThat(response.uploadId()).isEqualTo(uploadId.toString());
        assertThat(response.presignedUrls()).containsExactly("url-1", "url-2", "url-3", "url-4");
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.UPLOADING);
        verify(uploadDataOutput).save(upload);
        verify(uploadCacheOutput).save(uploadId.toString(), upload, 48L);
        verify(s3UploadOutput, times(4)).generatePresignedUrl(eq(upload), any(Integer.class));
    }

    @Test
    void shouldStartUploadWithNoPartsWhenFileSizeIsZero() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        StartUploadRequest request = new StartUploadRequest("user-1", "video.mp4", 0L, 5L);
        Upload upload = buildUpload(uploadId, "users/user-1/video.mp4", StatusEnum.PROCESSING);

        when(s3UploadOutput.startUpload(request)).thenReturn(upload);

        // Act
        StartUploadResponse response = uploadService.startUpload(request);

        // Assert
        assertThat(response.uploadId()).isEqualTo(uploadId.toString());
        assertThat(response.presignedUrls()).isEmpty();
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.UPLOADING);
        verify(s3UploadOutput, never()).generatePresignedUrl(any(), any(Integer.class));
    }

    @Test
    void shouldPropagateErrorWhenStartUploadFails() {
        // Arrange
        StartUploadRequest request = new StartUploadRequest("user-1", "video.mp4", 100L, 10L);
        when(s3UploadOutput.startUpload(request)).thenThrow(new RuntimeException("s3 unavailable"));

        // Act + Assert
        assertThatThrownBy(() -> uploadService.startUpload(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("s3 unavailable");

        verify(uploadDataOutput, never()).save(any());
        verify(uploadCacheOutput, never()).save(any(), any(), any());
    }

    @Test
    void shouldCompleteUploadUsingCachedUploadAndSendProcessMessage() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOADING);
        List<UploadPart> parts = List.of(new UploadPart(1, "etag-1"));

        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(upload);

        // Act
        uploadService.completeUpload(uploadId, parts);

        // Assert
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.UPLOADED);
        verify(s3UploadOutput).completeUpload(upload, parts);
        verify(uploadCacheOutput).delete(uploadId.toString());
        verify(uploadDataOutput).save(upload);

        ArgumentCaptor<ProcessRequest> processCaptor = ArgumentCaptor.forClass(ProcessRequest.class);
        verify(messageOutput).sendProcessMessage(processCaptor.capture());
        assertThat(processCaptor.getValue().getUploadId()).isEqualTo(uploadId);
        assertThat(processCaptor.getValue().getKey()).isEqualTo("videos/file.mp4");
    }

    @Test
    void shouldCompleteUploadUsingDatabaseWhenCacheMiss() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOADING);

        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(null);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        // Act
        uploadService.completeUpload(uploadId, List.of(new UploadPart(1, "etag-1")));

        // Assert
        verify(uploadCacheOutput).save(uploadId.toString(), upload, 48L);
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.UPLOADED);
    }

    @Test
    void shouldSetUploadErrorWhenCompleteUploadThrowsRuntimeException() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOADING);
        List<UploadPart> parts = List.of(new UploadPart(1, "etag-1"));

        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(upload);
        doThrow(new RuntimeException("complete failed")).when(s3UploadOutput).completeUpload(upload, parts);

        // Act
        uploadService.completeUpload(uploadId, parts);

        // Assert
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.UPLOAD_ERROR);
        verify(uploadDataOutput).save(upload);
        verify(uploadCacheOutput, never()).delete(uploadId.toString());
        verify(messageOutput, never()).sendProcessMessage(any());
    }

    @Test
    void shouldThrowWhenCompleteUploadAndUploadDoesNotExist() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(null);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uploadService.completeUpload(uploadId, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReprocessSuccessfullyWhenUploadExists() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOAD_ERROR);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        // Act
        uploadService.reprocess(uploadId);

        // Assert
        verify(messageOutput).sendProcessMessage(any(ProcessRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenReprocessUploadDoesNotExist() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uploadService.reprocess(uploadId))
                .isInstanceOf(UploadNotExistException.class)
                .hasMessageContaining(uploadId.toString());

        verify(messageOutput, never()).sendProcessMessage(any());
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOADING);
        StatusUpdate statusUpdate = new StatusUpdate(uploadId, StatusEnum.PROCESSED);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        // Act
        uploadService.updateStatus(statusUpdate);

        // Assert
        assertThat(upload.getStatus()).isEqualTo(StatusEnum.PROCESSED);
        verify(uploadDataOutput).save(upload);
    }

    @Test
    void shouldAllowNullStatusWhenUpdatingStatusAsEdgeCase() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        Upload upload = buildUpload(uploadId, "videos/file.mp4", StatusEnum.UPLOADING);
        StatusUpdate statusUpdate = new StatusUpdate(uploadId, null);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        // Act
        uploadService.updateStatus(statusUpdate);

        // Assert
        assertThat(upload.getStatus()).isNull();
        verify(uploadDataOutput).save(upload);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusForUnknownUpload() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        StatusUpdate statusUpdate = new StatusUpdate(uploadId, StatusEnum.PROCESSING);

        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uploadService.updateStatus(statusUpdate))
                .isInstanceOf(UploadNotExistException.class)
                .hasMessageContaining(uploadId.toString());
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
