package com.fiap.vp_upload.domain.service.impl;

import com.fiap.vp_upload.application.ports.output.UploadPartCacheOutput;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadPartServiceImplTest {

    @Mock private UploadPartCacheOutput uploadPartCacheOutput;
    @InjectMocks private UploadPartServiceImpl uploadPartService;

    @Test
    void findByUploadIdOrderByPartNumberShouldDelegateToCache() {
        UUID uploadId = UUID.randomUUID();
        List<UploadPart> parts = List.of(UploadPart.builder().partNumber(1).eTag("tag1").build());
        when(uploadPartCacheOutput.findByUploadIdOrderByPartNumber(uploadId.toString())).thenReturn(parts);

        assertEquals(parts, uploadPartService.findByUploadIdOrderByPartNumber(uploadId));
    }

    @Test
    void confirmPartUploadShouldSaveWithDefaultTtl() {
        UUID uploadId = UUID.randomUUID();
        UploadPartConfirmRequest req = new UploadPartConfirmRequest(2, "etag-2");

        uploadPartService.confirmPartUpload(uploadId, req);

        verify(uploadPartCacheOutput).save(uploadId.toString(), 2, "etag-2", 48L);
    }

    @Test
    void clearDataShouldDeleteAllPartsFromCache() {
        UUID uploadId = UUID.randomUUID();

        uploadPartService.clearData(uploadId);

        verify(uploadPartCacheOutput).deleteAll(uploadId.toString());
    }
}
