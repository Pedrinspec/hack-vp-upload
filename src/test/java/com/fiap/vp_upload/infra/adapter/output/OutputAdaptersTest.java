package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.infra.adapter.output.repository.UploadPartCacheRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.UploadRedisRepository;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.Upload;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.UploadPart;
import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutputAdaptersTest {

    @Mock private UploadRedisRepository uploadRedisRepository;
    @Mock private UploadPartCacheRepository uploadPartCacheRepository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private S3Client s3Client;

    @Test
    void uploadCacheOutputShouldPrefixKeysAndSerialize() {
        Gson gson = new Gson();
        UploadCacheOutputImpl output = new UploadCacheOutputImpl(gson, uploadRedisRepository);
        Upload upload = Upload.builder().uploadId(UUID.randomUUID()).key("k").status(StatusEnum.UPLOADING).build();

        output.save("id", upload, 48L);
        verify(uploadRedisRepository).save(eq("vp-upload:id"), anyString(), eq(48L));

        when(uploadRedisRepository.findByKey("vp-upload:id")).thenReturn(gson.toJson(upload));
        Upload found = output.findByKey("id");
        assertEquals(upload.getUploadId(), found.getUploadId());

        output.delete("id");
        verify(uploadRedisRepository).delete("vp-upload:id");
    }

    @Test
    void uploadPartCacheOutputShouldPrefixKeys() {
        UploadPartCacheOutputImpl output = new UploadPartCacheOutputImpl(uploadPartCacheRepository);
        List<UploadPart> parts = List.of(UploadPart.builder().partNumber(1).eTag("e").build());

        output.save("u1", 1, "e", 24L);
        verify(uploadPartCacheRepository).save("vp-upload:u1:parts", "1", "e", 24L);

        when(uploadPartCacheRepository.findByUploadIdOrderByPartNumber("vp-upload:u1:parts")).thenReturn(parts);
        assertEquals(parts, output.findByUploadIdOrderByPartNumber("u1"));

        output.deleteAll("u1");
        verify(uploadPartCacheRepository).deleteAll("vp-upload:u1:parts");
    }

    @Test
    void kafkaOutputShouldUseConfiguredTopics() {
        KafkaMessageOutputImpl output = new KafkaMessageOutputImpl(kafkaTemplate, new Gson());
        ReflectionTestUtils.setField(output, "processorTopic", "processor-topic");
        ReflectionTestUtils.setField(output, "notificationTopic", "notification-topic");

        output.sendProcessMessage(new com.fiap.vp_upload.domain.model.ProcessRequest(UUID.randomUUID(), "key"));
        output.sendFailMessage(com.fiap.vp_upload.domain.model.UploadError.builder().reason("r").details("d").build());

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(2)).send(captor.capture());
        assertEquals("processor-topic", captor.getAllValues().get(0).topic());
        assertEquals("notification-topic", captor.getAllValues().get(1).topic());
    }

    @Test
    void s3DownloadOutputShouldBuildGetObjectRequest() {
        S3DownloadOutputImpl output = new S3DownloadOutputImpl(s3Client);
        ReflectionTestUtils.setField(output, "videoBucket", "video-bucket");
        ResponseInputStream<GetObjectResponse> stream = mock(ResponseInputStream.class);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        ResponseInputStream<GetObjectResponse> result = output.downloadFile("a/b/c.zip");

        assertSame(stream, result);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertEquals("video-bucket", captor.getValue().bucket());
        assertEquals("a/b/c.zip", captor.getValue().key());
    }
}
