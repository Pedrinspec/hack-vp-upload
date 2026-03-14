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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadPartServiceImplTest {

    @Mock
    private UploadPartCacheOutput uploadPartCacheOutput;

    @InjectMocks
    private UploadPartServiceImpl uploadPartService;

    @Test
    void shouldReturnPartsOrderedByPartNumber() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        List<UploadPart> expected = List.of(
                new UploadPart(1, "etag-1"),
                new UploadPart(2, "etag-2")
        );
        when(uploadPartCacheOutput.findByUploadIdOrderByPartNumber(uploadId.toString())).thenReturn(expected);

        // Act
        List<UploadPart> result = uploadPartService.findByUploadIdOrderByPartNumber(uploadId);

        // Assert
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    void shouldReturnEmptyListWhenNoPartsWereUploadedYet() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        when(uploadPartCacheOutput.findByUploadIdOrderByPartNumber(uploadId.toString())).thenReturn(List.of());

        // Act
        List<UploadPart> result = uploadPartService.findByUploadIdOrderByPartNumber(uploadId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConfirmPartUploadWithGivenPartNumberAndETag() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        UploadPartConfirmRequest request = new UploadPartConfirmRequest(3, "etag-3");

        // Act
        uploadPartService.confirmPartUpload(uploadId, request);

        // Assert
        verify(uploadPartCacheOutput).save(uploadId.toString(), 3, "etag-3", 48L);
    }

    @Test
    void shouldConfirmPartUploadEvenWhenETagIsNullAsEdgeCase() {
        // Arrange
        UUID uploadId = UUID.randomUUID();
        UploadPartConfirmRequest request = new UploadPartConfirmRequest(3, null);

        // Act
        uploadPartService.confirmPartUpload(uploadId, request);

        // Assert
        verify(uploadPartCacheOutput).save(uploadId.toString(), 3, null, 48L);
    }

    @Test
    void shouldClearAllPartsFromCache() {
        // Arrange
        UUID uploadId = UUID.randomUUID();

        // Act
        uploadPartService.clearData(uploadId);

        // Assert
        verify(uploadPartCacheOutput).deleteAll(uploadId.toString());
    }

    @Test
    void shouldClearAllPartsFromCacheEvenWhenUploadIdHasNoData() {
        // Arrange
        UUID uploadId = UUID.randomUUID();

        // Act
        uploadPartService.clearData(uploadId);

        // Assert
        verify(uploadPartCacheOutput).deleteAll(uploadId.toString());
    }
}
