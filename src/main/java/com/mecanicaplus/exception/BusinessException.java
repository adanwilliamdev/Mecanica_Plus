package com.mecanicaplus.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
