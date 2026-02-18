package com.fiap.vp_upload.infra.adapter.input.mapper;

import com.fiap.vp_upload.domain.model.FinishUpload;
import com.fiap.vp_upload.infra.adapter.input.dto.FinishUploadRequest;
import org.springframework.stereotype.Component;

public class FinishUploadMapper {

    public static FinishUpload toModel(FinishUploadRequest dto){


        FinishUpload model = new FinishUpload();
        model.setUploadId(dto.getUploadId());
        model.setTotalChunks(dto.getTotalChunks());
        model.setOriginalFileName(dto.getOriginalFileName());
        return model;
    }
}
