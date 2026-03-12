package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.S3DownloadOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
@RequiredArgsConstructor
public class S3DownloadOutputImpl implements S3DownloadOutput {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket.video}")
    private String videoBucket;

    @Override
    public ResponseInputStream<GetObjectResponse> downloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(videoBucket)
                .key(key)
                .build();

        return s3Client.getObject(request);
    }
}
