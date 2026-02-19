package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3UploadAdapter implements S3UploadOutput {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.video}")
    private String videoBucket;

    @Override
    public Upload startUpload(StartUploadRequest request) {

        String uploadId = UUID.randomUUID().toString();

        String extension = extractExtension(request.originalFileName());

        String key = String.format(
                "user/%s/upload/%s/video/%s.%s",
                request.userId(),
                uploadId,
                extractFileName(request.originalFileName()),
                extension
        );

        CreateMultipartUploadRequest createRequest =
                CreateMultipartUploadRequest.builder()
                        .bucket(videoBucket)
                        .key(key)
                        .build();

        CreateMultipartUploadResponse response =
                s3Client.createMultipartUpload(createRequest);

        return Upload.builder()
                .uploadId(UUID.fromString(uploadId))
                .s3UploadId(response.uploadId())
                .key(response.key())
                .userId(request.userId())
                .status("STARTED")
                .build();
    }

    @Override
    public void completeUpload(Upload upload, List<UploadPart> parts) {

        if (parts.isEmpty()) {
            throw new RuntimeException("Nenhuma parte enviada");
        }

        List<CompletedPart> completedParts = parts.stream()
                .map(part -> CompletedPart.builder()
                        .partNumber(part.getPartNumber())
                        .eTag(part.getETag())
                        .build())
                .toList();

        CompletedMultipartUpload completedMultipartUpload =
                CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build();

        CompleteMultipartUploadRequest completeRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(videoBucket)
                        .key(upload.getKey())
                        .uploadId(upload.getS3UploadId())
                        .multipartUpload(completedMultipartUpload)
                        .build();

        s3Client.completeMultipartUpload(completeRequest);
    }

    @Override
    public String generatePresignedUrl(Upload upload, int partNumber) {

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(videoBucket)
                .key(upload.getKey())
                .uploadId(upload.getS3UploadId())
                .partNumber(partNumber)
                .build();

        PresignedUploadPartRequest presignedRequest =
                s3Presigner.presignUploadPart(
                        UploadPartPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))
                                .uploadPartRequest(uploadPartRequest)
                                .build()
                );

        return presignedRequest.url().toString();
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            throw new IllegalArgumentException("Arquivo sem extensão");
        }
        return fileName.substring(lastDot + 1);
    }

    private String extractFileName(String fileName) {
        int lastDot = fileName.lastIndexOf(".");

        return fileName.substring(0, lastDot);
    }
}
