package com.fiap.vp_upload.domain.exceptions;

import java.util.UUID;

public class NoVideoPartsProvidedException extends GenericUploadException {
    public NoVideoPartsProvidedException(UUID uploadId) {
        super(uploadId, "Nenhuma parte enviada");
    }
}
