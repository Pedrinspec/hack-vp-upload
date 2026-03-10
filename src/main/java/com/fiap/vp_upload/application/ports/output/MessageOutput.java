package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.fiap.vp_upload.domain.model.UploadError;

public interface MessageOutput {
    void sendProcessMessage(ProcessRequest processRequest);

    void sendFailMessage(UploadError uploadError);
}
