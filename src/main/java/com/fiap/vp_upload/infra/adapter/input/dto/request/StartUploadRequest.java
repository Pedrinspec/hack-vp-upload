package com.fiap.vp_upload.infra.adapter.input.dto.request;

public record StartUploadRequest(
        String userId,
        String originalFileName
) {
}
