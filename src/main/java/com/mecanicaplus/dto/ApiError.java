package com.mecanicaplus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/** Corpo padrão de erro da API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(Instant timestamp, int status, String message, List<String> errors) {

    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message, null);
    }

    public static ApiError ofErrors(int status, List<String> errors) {
        return new ApiError(Instant.now(), status, null, errors);
    }
}
