package com.fiap.vp_upload.infra.exceptions;

import com.fiap.vp_upload.domain.exceptions.NoSuchChunkException;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.infra.adapter.input.dto.response.CustomErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChunkUploadExceptionHandlerController {

    @ExceptionHandler(NoSuchChunkException.class)
    public ResponseEntity<CustomErrorResponseDto> handleInvalidChunkQuantityException(NoSuchChunkException ex){
        CustomErrorResponseDto errorResponseDto = new CustomErrorResponseDto();
        errorResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponseDto.setMessage("Contagem de chunks divergente");
        errorResponseDto.setDescription(ex.getMessage());
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
}
