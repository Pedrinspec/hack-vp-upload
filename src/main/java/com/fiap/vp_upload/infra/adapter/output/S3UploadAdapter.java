package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.S3UploadOutput;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadPartRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3UploadAdapter implements S3UploadOutput {

    private final S3Client s3Client;
    private final UploadRepository uploadRepository;
    private final UploadPartRepository uploadPartRepository;

    @Value("${aws.s3.bucket.video}")
    private String videoBucket;

    @Override
    public StartUploadResponse startUpload(StartUploadRequest request) {

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

        uploadRepository.save(
                Upload.builder()
                        .uploadId(UUID.fromString(uploadId))
                        .s3UploadId(response.uploadId())
                        .key(response.key())
                        .userId(request.userId())
                        .status("STARTED")
                        .build()
        );

        return new StartUploadResponse(uploadId);
    }

    @Override
    public void uploadPart(UUID uploadId, int partNumber, MultipartFile file) {

        Upload upload = uploadRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new RuntimeException("Upload não encontrado"));

        try {

            UploadPartRequest uploadPartRequest =
                    UploadPartRequest.builder()
                            .bucket(videoBucket)
                            .key(upload.getKey())
                            .uploadId(upload.getS3UploadId())
                            .partNumber(partNumber)
                            .build();

            UploadPartResponse response = s3Client.uploadPart(
                    uploadPartRequest,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            uploadPartRepository.save(UploadPart.builder()
                    .partNumber(partNumber)
                    .uploadId(uploadId)
                    .eTag(response.eTag())
                    .build());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completeUpload(UUID uploadId) {

        Upload upload = uploadRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new RuntimeException("Upload não encontrado"));

        List<UploadPart> parts = uploadPartRepository
                .findByUploadIdOrderByPartNumber(uploadId);

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

        upload.setStatus("COMPLETED");
        uploadRepository.save(upload);
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
