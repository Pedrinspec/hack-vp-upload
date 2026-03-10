package com.fiap.vp_upload.infra.exceptions;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.domain.exceptions.GenericUploadException;
import com.fiap.vp_upload.domain.exceptions.InvalidPartNumberException;
import com.fiap.vp_upload.domain.exceptions.NoSuchChunkException;
import com.fiap.vp_upload.domain.exceptions.NoSuchVideoExtensionException;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.UploadError;
import com.fiap.vp_upload.infra.adapter.input.dto.response.CustomErrorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ChunkUploadExceptionHandlerController {

    private final MessageOutput messageOutput;

    @ExceptionHandler(NoSuchChunkException.class)
    public ResponseEntity<CustomErrorResponseDto> handleInvalidChunkQuantityException(NoSuchChunkException ex){
        CustomErrorResponseDto errorResponseDto = new CustomErrorResponseDto();
        errorResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponseDto.setMessage("Contagem de chunks divergente");
        errorResponseDto.setDescription(ex.getMessage());

        sendFailMessage(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(UploadNotExistException.class)
    public ResponseEntity<CustomErrorResponseDto> handleUploadNotExistException(UploadNotExistException ex){
        CustomErrorResponseDto errorResponseDto = new CustomErrorResponseDto();
        errorResponseDto.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorResponseDto.setMessage("Upload não existe");
        errorResponseDto.setDescription(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }

    @ExceptionHandler(NoSuchVideoExtensionException.class)
    public ResponseEntity<CustomErrorResponseDto> handleNoSuchVideoExtensionException(NoSuchVideoExtensionException ex){
        CustomErrorResponseDto errorResponseDto = new CustomErrorResponseDto();
        errorResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponseDto.setMessage(ex.getMessage());
        errorResponseDto.setDescription(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(InvalidPartNumberException.class)
    public ResponseEntity<CustomErrorResponseDto> handleInvalidPartNumberException(InvalidPartNumberException ex){
        CustomErrorResponseDto errorResponseDto = new CustomErrorResponseDto();
        errorResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponseDto.setMessage(ex.getMessage());
        errorResponseDto.setDescription(ex.getMessage());

        sendFailMessage(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    private void sendFailMessage(GenericUploadException ex){
        messageOutput.sendFailMessage(UploadError.builder()
                .videoId(ex.getUploadId())
                .reason(ex.getClass().getName())
                .details(ex.getMessage())
                .build());
    }

}
