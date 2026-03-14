package com.fiap.vp_upload.infra.adapter.input.mapper;

import com.fiap.vp_upload.infra.adapter.input.dto.request.FinishUploadRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinishUploadMapperTest {

    @Test
    void toModelShouldMapAllFields() {
        UUID uploadId = UUID.randomUUID();
        FinishUploadRequest dto = new FinishUploadRequest();
        dto.setUploadId(uploadId);
        dto.setOriginalFileName("video.mp4");
        dto.setTotalChunks(12);

        var model = FinishUploadMapper.toModel(dto);

        assertEquals(uploadId, model.getUploadId());
        assertEquals("video.mp4", model.getOriginalFileName());
        assertEquals(12, model.getTotalChunks());
    }
}
