package com.mecanicaplus.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mecanicaplus.dto.ApiError;
import com.mecanicaplus.exception.AppException;
import com.mecanicaplus.exception.ForbiddenException;
import com.mecanicaplus.exception.OptimisticLockConflictException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Traduz exceções em respostas JSON no formato padrão da API. */
@RestControllerAdvice(basePackages = "com.mecanicaplus.api")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    static final String INTEGRITY_MESSAGE =
            "Operação viola uma restrição de integridade dos dados "
                    + "(ex.: registro duplicado ou vínculo existente)";

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiError> handleApp(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ApiError.of(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return validation(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> handleParamValidation(HandlerMethodValidationException ex) {
        List<String> errors = ex.getAllValidationResults().stream()
                .flatMap(r -> {
                    String name = r.getMethodParameter().getParameterName();
                    return r.getResolvableErrors().stream()
                            .map(e -> (name == null ? "" : name + ": ") + e.getDefaultMessage());
                })
                .toList();
        return validation(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return validation(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            return validation(List.of((field == null ? "" : field + ": ") + "valor inválido"));
        }
        return validation(List.of("corpo da requisição inválido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return validation(List.of(ex.getName() + ": valor inválido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        return validation(List.of(ex.getParameterName() + ": campo obrigatório"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ForbiddenException.DEFAULT_MESSAGE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT, OptimisticLockConflictException.MESSAGE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, INTEGRITY_MESSAGE);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Erro não tratado ao processar requisição", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), message));
    }

    private static ResponseEntity<ApiError> validation(List<String> errors) {
        return ResponseEntity.unprocessableEntity().body(ApiError.ofErrors(422, errors));
    }
}
