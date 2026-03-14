package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.DownloadUseCase;
import com.fiap.vp_upload.application.usecase.UploadUseCase;
import com.fiap.vp_upload.domain.model.DownloadFile;
import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.fiap.vp_upload.domain.model.VideoView;
import com.fiap.vp_upload.infra.adapter.input.dto.request.StartUploadRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.request.UploadPartConfirmRequest;
import com.fiap.vp_upload.infra.adapter.input.dto.response.StartUploadResponse;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InputAdaptersTest {

    @Mock private UploadUseCase uploadUseCase;
    @Mock private DownloadUseCase downloadUseCase;

    @Test
    void uploadControllerShouldDelegateAndReturnExpectedStatuses() {
        UploadController controller = new UploadController(uploadUseCase);
        StartUploadRequest startReq = new StartUploadRequest(UUID.randomUUID(), "v.mp4", 10, 2);
        when(uploadUseCase.startUpload(startReq)).thenReturn(new StartUploadResponse("id", List.of("u1")));

        assertEquals(HttpStatus.OK, controller.startUpload(startReq).getStatusCode());

        UUID uploadId = UUID.randomUUID();
        assertEquals(HttpStatus.CREATED, controller.finishUpload(uploadId.toString()).getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, controller.confirmPart(uploadId.toString(), new UploadPartConfirmRequest(1, "e")).getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, controller.reprocess(uploadId.toString()).getStatusCode());

        verify(uploadUseCase).completeUpload(uploadId);
        verify(uploadUseCase).confirmPartUpload(eq(uploadId), any(UploadPartConfirmRequest.class));
        verify(uploadUseCase).reprocess(uploadId);
    }

    @Test
    void downloadControllerShouldReturnFileAndValidateUser() throws Exception {
        DownloadController controller = new DownloadController(downloadUseCase);
        UUID uploadId = UUID.randomUUID();
        DownloadFile file = DownloadFile.builder()
                .fileName("out.zip")
                .file(mock(software.amazon.awssdk.core.ResponseInputStream.class))
                .build();
        when(downloadUseCase.downloadFile(uploadId)).thenReturn(file);

        var response = controller.download(uploadId.toString());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, controller.getVideoList(" ").getStatusCode());

        UUID userId = UUID.randomUUID();
        when(downloadUseCase.getVideoList(userId)).thenReturn(List.of(new VideoView(uploadId, "v.mp4", "ok")));
        assertEquals(HttpStatus.OK, controller.getVideoList(userId.toString()).getStatusCode());
    }

    @Test
    void statusListenerShouldConvertJsonAndCallUseCase() {
        UploadUseCase useCase = mock(UploadUseCase.class);
        StatusListener listener = new StatusListener(new Gson(), useCase);
        StatusUpdate update = new StatusUpdate(UUID.randomUUID(), com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum.PROCESSED);

        listener.statusListener(new Gson().toJson(update));

        verify(useCase).updateStatus(any(StatusUpdate.class));
    }

    @Test
    void viewControllerShouldReturnUploadView() {
        assertEquals("upload", new ViewController().upload());
    }
}
