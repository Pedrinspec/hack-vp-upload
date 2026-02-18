package com.fiap.vp_upload.domain.exceptions;

public class InvalidChunkQuantityException extends RuntimeException {
    public InvalidChunkQuantityException() {
        super("Quantidade de chunk inválida");
    }

    public InvalidChunkQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
