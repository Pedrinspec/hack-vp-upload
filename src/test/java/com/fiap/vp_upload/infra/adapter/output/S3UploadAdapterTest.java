package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.domain.exceptions.NoSuchVideoExtensionException;
import com.fiap.vp_upload.domain.exceptions.NoVideoPartsProvidedException;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3UploadAdapterTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner s3Presigner;
    @InjectMocks private S3UploadAdapter adapter;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(adapter, "videoBucket", "video-bucket");
    }

    @Test
    void startUploadShouldCreateMultipartUploadAndBuildUploadObject() {
        StartUploadRequest request = new StartUploadRequest(UUID.randomUUID(), "movie.mp4", 100L, 10);
        when(s3Client.createMultipartUpload(any())).thenReturn(CreateMultipartUploadResponse.builder()
                .uploadId("aws-upload-id")
                .key("returned/key.mp4")
                .build());

        Upload result = adapter.startUpload(request);

        assertEquals("aws-upload-id", result.getS3UploadId());
        assertEquals("returned/key.mp4", result.getKey());
        assertEquals(request.userId().toString(), result.getUserId());
        assertEquals(StatusEnum.UPLOADING, result.getStatus());
    }

    @Test
    void startUploadShouldThrowWhenFileHasNoExtension() {
        StartUploadRequest request = new StartUploadRequest(UUID.randomUUID(), "movie", 100L, 10);

        assertThrows(NoSuchVideoExtensionException.class, () -> adapter.startUpload(request));
    }

    @Test
    void completeUploadShouldThrowWhenNoParts() {
        Upload upload = Upload.builder().uploadId(UUID.randomUUID()).build();

        assertThrows(NoVideoPartsProvidedException.class, () -> adapter.completeUpload(upload, List.of()));
    }

    @Test
    void completeUploadShouldSendMappedPartsToS3() {
        Upload upload = Upload.builder().uploadId(UUID.randomUUID()).key("key").s3UploadId("s3-id").build();
        List<UploadPart> parts = List.of(
                UploadPart.builder().partNumber(2).eTag("e2").build(),
                UploadPart.builder().partNumber(1).eTag("e1").build());

        adapter.completeUpload(upload, parts);

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
        verify(s3Client).completeMultipartUpload(captor.capture());
        CompleteMultipartUploadRequest req = captor.getValue();
        assertEquals("video-bucket", req.bucket());
        assertEquals("key", req.key());
        assertEquals("s3-id", req.uploadId());
        assertEquals(2, req.multipartUpload().parts().size());
    }

    @Test
    void generatePresignedUrlShouldReturnUrlFromPresigner() {
        Upload upload = Upload.builder().key("k").s3UploadId("s3").build();
        PresignedUploadPartRequest presigned = mock(PresignedUploadPartRequest.class);
        when(presigned.url()).thenReturn(URI.create("https://example.com/presigned"));
        when(s3Presigner.presignUploadPart(any())).thenReturn(presigned);

        String url = adapter.generatePresignedUrl(upload, 4);

        assertEquals("https://example.com/presigned", url);
    }
}
