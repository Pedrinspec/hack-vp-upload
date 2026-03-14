package com.fiap.vp_upload.infra.exceptions;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.exceptions.NoSuchChunkException;
import com.fiap.vp_upload.domain.exceptions.NoSuchVideoExtensionException;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChunkUploadExceptionHandlerControllerTest {

    @Mock private MessageOutput messageOutput;
    @InjectMocks private ChunkUploadExceptionHandlerController handler;

    @Test
    void handleNoSuchChunkShouldReturnBadRequestAndSendMessage() {
        var response = handler.handleInvalidChunkQuantityException(new NoSuchChunkException(UUID.randomUUID()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Contagem de chunks divergente", response.getBody().getMessage());
        verify(messageOutput, times(1)).sendFailMessage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handleUploadNotExistShouldReturnNotFound() {
        var response = handler.handleUploadNotExistException(new UploadNotExistException("id"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Upload não existe", response.getBody().getMessage());
    }

    @Test
    void handleNoSuchVideoExtensionShouldReturnBadRequest() {
        var response = handler.handleNoSuchVideoExtensionException(new NoSuchVideoExtensionException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Arquivo sem extensão", response.getBody().getMessage());
    }

    @Test
    void handleInvalidPartShouldReturnBadRequestAndSendMessage() {
        var response = handler.handleInvalidPartNumberException(new InvalidPartNumberException(UUID.randomUUID()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(messageOutput, times(1)).sendFailMessage(org.mockito.ArgumentMatchers.any());
    }
}
