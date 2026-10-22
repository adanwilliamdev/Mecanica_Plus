package com.mecanicaplus.exception;

import org.springframework.http.HttpStatus;

/** Erro de negócio com status HTTP associado. */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
