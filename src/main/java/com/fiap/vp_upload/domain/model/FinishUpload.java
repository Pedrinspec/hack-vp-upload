package com.fiap.vp_upload.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class FinishUpload {

    private UUID uploadId;
    private int totalChunks;
    private String originalFileName;
}
