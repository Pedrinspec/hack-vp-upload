package com.fiap.vp_upload.domain.exceptions;

public class NoSuchChunkException extends RuntimeException {
    public NoSuchChunkException() {
        super("Contagem de chunks divergente");
    }

    public NoSuchChunkException(String message, Throwable cause) {
        super(message, cause);
    }
}
