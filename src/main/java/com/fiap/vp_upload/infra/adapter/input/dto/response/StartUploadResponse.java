package com.fiap.vp_upload.infra.adapter.input.dto.response;

import java.util.List;

public record StartUploadResponse(
        String uploadId, List<String> presignedUrls
) {
}
