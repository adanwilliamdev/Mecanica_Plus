package com.mecanicaplus.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends AppException {

    public static final String DEFAULT_MESSAGE = "Muitas tentativas. Tente novamente em instantes";

    public TooManyRequestsException() {
        super(HttpStatus.TOO_MANY_REQUESTS, DEFAULT_MESSAGE);
    }
}
