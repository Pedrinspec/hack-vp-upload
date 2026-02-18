package com.fiap.vp_upload.domain.exceptions;

public class UploadNotExistException extends RuntimeException {
    public UploadNotExistException(String uploadId) {
        super(String.format("Upload %s não existe", uploadId));
    }
}
