package com.fiap.vp_upload.application.ports.output;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public interface S3DownloadOutput {
    ResponseInputStream<GetObjectResponse> downloadFile(String key);
}
