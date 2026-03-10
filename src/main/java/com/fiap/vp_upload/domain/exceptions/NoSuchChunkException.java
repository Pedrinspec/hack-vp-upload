package com.fiap.vp_upload.domain.exceptions;

import lombok.Getter;

import java.util.UUID;

public class NoSuchChunkException extends GenericUploadException {


    public NoSuchChunkException(UUID uploadId) {
        super(uploadId, "Contagem de chunks divergente");
    }

}
