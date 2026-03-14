package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.application.ports.output.UploadCacheOutput;
import com.fiap.vp_upload.application.ports.output.UploadDataOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock private S3UploadOutput s3UploadOutput;
    @Mock private UploadDataOutput uploadDataOutput;
    @Mock private UploadCacheOutput uploadCacheOutput;
    @Mock private MessageOutput messageOutput;
    @InjectMocks private UploadServiceImpl uploadService;

    @Test
    void startUploadShouldGeneratePresignedUrlsAndPersistUpload() {
        StartUploadRequest request = new StartUploadRequest(UUID.randomUUID(), "video.mp4", 10, 4);
        Upload upload = Upload.builder().uploadId(UUID.randomUUID()).s3UploadId("s3").key("key/video.mp4").userId(request.userId().toString()).build();

        when(s3UploadOutput.startUpload(request)).thenReturn(upload);
        when(s3UploadOutput.generatePresignedUrl(upload, 1)).thenReturn("url-1");
        when(s3UploadOutput.generatePresignedUrl(upload, 2)).thenReturn("url-2");
        when(s3UploadOutput.generatePresignedUrl(upload, 3)).thenReturn("url-3");

        var response = uploadService.startUpload(request);

        assertEquals(upload.getUploadId().toString(), response.uploadId());
        assertEquals(List.of("url-1", "url-2", "url-3"), response.partsUrl());
        assertEquals(StatusEnum.UPLOADING, upload.getStatus());
        verify(uploadDataOutput).save(upload);
        verify(uploadCacheOutput).save(upload.getUploadId().toString(), upload, 48L);
    }

    @Test
    void completeUploadShouldUseCacheAndSendMessageOnSuccess() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).key("k.mp4").s3UploadId("s3").build();
        List<UploadPart> parts = List.of(UploadPart.builder().partNumber(1).eTag("tag").build());
        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(upload);

        uploadService.completeUpload(uploadId, parts);

        verify(s3UploadOutput).completeUpload(upload, parts);
        verify(uploadCacheOutput).delete(uploadId.toString());
        assertEquals(StatusEnum.UPLOADED, upload.getStatus());
        verify(uploadDataOutput).save(upload);
        verify(messageOutput).sendProcessMessage(any(ProcessRequest.class));
    }

    @Test
    void completeUploadShouldMarkErrorWhenS3Fails() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).key("k.mp4").s3UploadId("s3").build();
        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(upload);
        doThrow(new RuntimeException("boom")).when(s3UploadOutput).completeUpload(eq(upload), anyList());

        uploadService.completeUpload(uploadId, List.of());

        assertEquals(StatusEnum.UPLOAD_ERROR, upload.getStatus());
        verify(uploadDataOutput).save(upload);
        verify(messageOutput, never()).sendProcessMessage(any());
    }

    @Test
    void completeUploadShouldLoadFromDatabaseAndFillCacheWhenMissingInCache() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).key("k.mp4").s3UploadId("s3").build();
        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(null);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        uploadService.completeUpload(uploadId, List.of(UploadPart.builder().partNumber(1).eTag("tag").build()));

        verify(uploadCacheOutput).save(uploadId.toString(), upload, 48L);
    }

    @Test
    void completeUploadShouldThrowWhenUploadDoesNotExist() {
        UUID uploadId = UUID.randomUUID();
        when(uploadCacheOutput.findByKey(uploadId.toString())).thenReturn(null);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> uploadService.completeUpload(uploadId, List.of()));
    }

    @Test
    void startUploadShouldThrowInvalidPartNumberWhenChunkSizeIsNegative() {
        StartUploadRequest request = new StartUploadRequest(UUID.randomUUID(), "video.mp4", 10, -5);
        Upload upload = Upload.builder().uploadId(UUID.randomUUID()).s3UploadId("s3").key("key/video.mp4").userId("u").build();
        when(s3UploadOutput.startUpload(request)).thenReturn(upload);

        assertThrows(InvalidPartNumberException.class, () -> uploadService.startUpload(request));
        assertEquals(StatusEnum.UPLOAD_ERROR, upload.getStatus());
        verify(uploadDataOutput, atLeastOnce()).save(upload);
    }

    @Test
    void reprocessShouldSendMessageWhenUploadExists() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).key("k.mp4").build();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        uploadService.reprocess(uploadId);

        verify(messageOutput).sendProcessMessage(any(ProcessRequest.class));
    }

    @Test
    void reprocessShouldThrowWhenUploadDoesNotExist() {
        UUID uploadId = UUID.randomUUID();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        assertThrows(UploadNotExistException.class, () -> uploadService.reprocess(uploadId));
    }

    @Test
    void updateStatusShouldSaveNewStatus() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = Upload.builder().uploadId(uploadId).status(StatusEnum.UPLOADING).build();
        StatusUpdate statusUpdate = new StatusUpdate(uploadId, StatusEnum.PROCESSED);
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.of(upload));

        uploadService.updateStatus(statusUpdate);

        assertEquals(StatusEnum.PROCESSED, upload.getStatus());
        verify(uploadDataOutput).save(upload);
    }

    @Test
    void updateStatusShouldThrowWhenUploadDoesNotExist() {
        UUID uploadId = UUID.randomUUID();
        when(uploadDataOutput.findByUploadId(uploadId)).thenReturn(Optional.empty());

        assertThrows(UploadNotExistException.class, () -> uploadService.updateStatus(new StatusUpdate(uploadId, StatusEnum.PROCESSED)));
    }
}
