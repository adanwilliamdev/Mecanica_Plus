package com.torquegestao.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {

    public static final String DEFAULT_MESSAGE = "Você não tem permissão para executar esta ação";

    public ForbiddenException() {
        this(DEFAULT_MESSAGE);
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
