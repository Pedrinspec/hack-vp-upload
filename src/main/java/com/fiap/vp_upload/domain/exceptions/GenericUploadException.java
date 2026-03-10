package com.fiap.vp_upload.domain.exceptions;

import lombok.Getter;

import java.util.UUID;

@Getter
public class GenericUploadException extends RuntimeException {
    public final UUID uploadId;

    public GenericUploadException(UUID uploadId, String message) {
        super();
        this.uploadId = uploadId;
    }
}
