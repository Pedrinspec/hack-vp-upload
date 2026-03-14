package com.fiap.vp_upload.application.ports.input;

import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.VideoView;
import com.fiap.vp_upload.domain.service.DownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadInputPortTest {

    @Mock private DownloadService downloadService;
    @InjectMocks private DownloadInputPort downloadInputPort;

    @Test
    void downloadFileShouldDelegate() {
        UUID uploadId = UUID.randomUUID();
        DownloadFile file = DownloadFile.builder().fileName("a.zip").build();
        when(downloadService.downloadFile(uploadId)).thenReturn(file);

        assertEquals(file, downloadInputPort.downloadFile(uploadId));
    }

    @Test
    void getVideoListShouldDelegate() {
        UUID userId = UUID.randomUUID();
        List<VideoView> views = List.of(new VideoView(UUID.randomUUID(), "v.mp4", "ok"));
        when(downloadService.getVideoList(userId)).thenReturn(views);

        assertEquals(views, downloadInputPort.getVideoList(userId));
    }
}
