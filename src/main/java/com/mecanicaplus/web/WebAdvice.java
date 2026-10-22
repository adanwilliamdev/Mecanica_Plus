package com.mecanicaplus.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.mecanicaplus.exception.AppException;
import com.mecanicaplus.exception.OptimisticLockConflictException;

/** Atributos comuns de todas as páginas e tratamento de erros de negócio nas ações (POST). */
@ControllerAdvice(basePackages = "com.mecanicaplus.web")
public class WebAdvice {

    static final String INTEGRITY_MESSAGE =
            "Operação viola uma restrição de integridade dos dados "
                    + "(ex.: registro duplicado ou vínculo existente)";

    /** Caminho atual, usado para destacar o item ativo do menu. */
    @ModelAttribute("currentPath")
    String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(AppException.class)
    String handleApp(AppException ex, HttpServletRequest request) {
        return flashError(ex.getMessage(), request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    String handleOptimistic(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return flashError(OptimisticLockConflictException.MESSAGE, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    String handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return flashError(INTEGRITY_MESSAGE, request);
    }

    private static String flashError(String message, HttpServletRequest request) {
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("error", message);
        return "redirect:" + backTarget(request);
    }

    /** Volta para a página de origem (somente caminho + query, nunca outro host). */
    static String backTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                String path = uri.getRawPath();
                if (path != null && path.startsWith("/") && !path.startsWith("//")) {
                    return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
                }
            } catch (IllegalArgumentException ignored) {
                // Referer malformado: cai no padrão abaixo.
            }
        }
        return "/";
    }
}
