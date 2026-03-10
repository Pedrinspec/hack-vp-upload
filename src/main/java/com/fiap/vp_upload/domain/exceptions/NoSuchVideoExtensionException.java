package com.fiap.vp_upload.domain.exceptions;

public class NoSuchVideoExtensionException extends RuntimeException {

    public NoSuchVideoExtensionException() {
        super("Arquivo sem extensão");
    }
}
