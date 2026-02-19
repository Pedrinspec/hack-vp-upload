package com.fiap.vp_upload.infra.adapter.input.dto.request;

public record UploadPartConfirmRequest(int partNumber, String eTag) {
}
