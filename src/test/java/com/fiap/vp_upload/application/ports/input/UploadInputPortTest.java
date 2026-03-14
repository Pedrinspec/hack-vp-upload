package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.fiap.vp_upload.domain.service.UploadPartService;
import com.fiap.vp_upload.domain.service.UploadService;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadInputPortTest {

    @Mock private UploadService uploadService;
    @Mock private UploadPartService uploadPartService;
    @InjectMocks private UploadInputPort uploadInputPort;

    @Test
    void startUploadShouldDelegateToService() {
        StartUploadRequest req = new StartUploadRequest(UUID.randomUUID(), "v.mp4", 100, 10);
        StartUploadResponse response = new StartUploadResponse("id", List.of("u1"));
        when(uploadService.startUpload(req)).thenReturn(response);

        assertEquals(response, uploadInputPort.startUpload(req));
    }

    @Test
    void completeUploadShouldFetchPartsCompleteAndClearCache() {
        UUID uploadId = UUID.randomUUID();
        List<UploadPart> parts = List.of(UploadPart.builder().partNumber(1).eTag("e").build());
        when(uploadPartService.findByUploadIdOrderByPartNumber(uploadId)).thenReturn(parts);

        uploadInputPort.completeUpload(uploadId);

        verify(uploadService).completeUpload(uploadId, parts);
        verify(uploadPartService).clearData(uploadId);
    }

    @Test
    void confirmPartUploadShouldDelegate() {
        UUID uploadId = UUID.randomUUID();
        UploadPartConfirmRequest req = new UploadPartConfirmRequest(1, "e");

        uploadInputPort.confirmPartUpload(uploadId, req);

        verify(uploadPartService).confirmPartUpload(uploadId, req);
    }

    @Test
    void reprocessAndUpdateStatusShouldDelegate() {
        UUID uploadId = UUID.randomUUID();
        StatusUpdate statusUpdate = new StatusUpdate(uploadId, StatusEnum.PROCESSING);

        uploadInputPort.reprocess(uploadId);
        uploadInputPort.updateStatus(statusUpdate);

        verify(uploadService).reprocess(uploadId);
        verify(uploadService).updateStatus(statusUpdate);
    }
}
