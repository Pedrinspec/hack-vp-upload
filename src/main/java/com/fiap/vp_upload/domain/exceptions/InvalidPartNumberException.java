package com.fiap.vp_upload.domain.exceptions;

public class InvalidPartNumberException extends RuntimeException {
    public InvalidPartNumberException() {
        super("Quantidade de chunk inválida");
    }

    public InvalidPartNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
