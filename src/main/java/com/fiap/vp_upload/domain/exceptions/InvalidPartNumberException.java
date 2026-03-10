package com.fiap.vp_upload.domain.exceptions;

import java.util.UUID;

public class InvalidPartNumberException extends GenericUploadException {

    public InvalidPartNumberException(UUID uploadId) {
        super(uploadId, "Quantidade de chunk inválida");
    }
}
