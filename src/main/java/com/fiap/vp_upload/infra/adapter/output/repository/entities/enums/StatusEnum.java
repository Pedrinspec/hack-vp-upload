package com.fiap.vp_upload.infra.adapter.output.repository.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    UPLOADING("uploading", "carregando"),
    UPLOADED("uploaded", "carregado"),
    PROCESSING("processing", "processando"),
    PROCESSED("processed", "processado"),
    UPLOAD_ERROR("upload error", "erro no carregamento"),
    PROCESSING_ERROR("processing error", "erro no processamento"),
    ;
    private final String value;
    private final String description;
}
