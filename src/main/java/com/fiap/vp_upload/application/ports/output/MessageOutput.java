package com.fiap.vp_upload.application.ports.output;

import com.fiap.vp_upload.domain.model.ProcessRequest;

public interface MessageOutput {
    void sendProcessMessage(ProcessRequest processRequest);
}
