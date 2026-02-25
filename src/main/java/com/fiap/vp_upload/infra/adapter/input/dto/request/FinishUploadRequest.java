package com.fiap.vp_upload.infra.adapter.input.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class FinishUploadRequest {

    private UUID uploadId;
    private int totalChunks;
    private String originalFileName;
}
