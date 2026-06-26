package com.dms.exception;

import com.dms.constant.ErrorConstants;
import com.dms.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 400 Validation ───────────────────────────────────────────────────────

    /**
     * Handles {@code @Valid} / {@code @Validated} bean validation failures.
     * Returns one {@link ApiResponse.FieldError} per failing field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return ApiResponse.FieldError.builder()
                                .field(fe.getField())
                                .message(fe.getDefaultMessage())
                                .rejectedValue(fe.getRejectedValue())
                                .build();
                    }
                    return ApiResponse.FieldError.builder()
                            .field(error.getObjectName())
                            .message(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        log.warn("Validation failed for [{}]: {} error(s)", request.getRequestURI(), fieldErrors.size());

        return buildResponse(HttpStatus.BAD_REQUEST,
                ErrorConstants.VALIDATION_FAILED, fieldErrors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON request to [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
                ErrorConstants.MALFORMED_REQUEST, null, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, message, null, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(HttpStatus.BAD_REQUEST, message, null, request);
    }

    // ─── 401 Authentication ───────────────────────────────────────────────────

    @ExceptionHandler(com.dms.exception.InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            com.dms.exception.InvalidCredentialsException ex, HttpServletRequest request) {

        log.warn("Authentication failure at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED,
                ErrorConstants.INVALID_CREDENTIALS, null, request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(
            DisabledException ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED,
                ErrorConstants.ACCOUNT_DISABLED, null, request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockedAccount(
            LockedException ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED,
                ErrorConstants.ACCOUNT_LOCKED, null, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("AuthenticationException at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED,
                ErrorConstants.AUTHENTICATION_REQUIRED, null, request);
    }

    // ─── 403 Authorization ────────────────────────────────────────────────────

    /**
     * Spring Security's own {@link AccessDeniedException} (thrown by method security).
     * Note: our custom {@code com.dms.exception.AccessDeniedException} is handled separately.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied to [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN,
                ErrorConstants.ACCESS_DENIED, null, request);
    }

    @ExceptionHandler(com.dms.exception.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomAccessDenied(
            com.dms.exception.AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Custom access denied at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null, request);
    }

    // ─── 404 Not Found ────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.info("Resource not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        String message = String.format("No endpoint found for %s %s",
                ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(HttpStatus.NOT_FOUND, message, null, request);
    }

    // ─── 405 Method Not Allowed ───────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Supported: %s",
                ex.getMethod(),
                ex.getSupportedHttpMethods());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message, null, request);
    }

    // ─── 409 Conflict ─────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.info("Duplicate resource at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null, request);
    }

    // ─── 422 Business Rule ────────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.info("Business rule violation at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null, request);
    }

    // ─── 500 Catch-all ────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaught(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorConstants.INTERNAL_ERROR, null, request);
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            String message,
            List<ApiResponse.FieldError> errors,
            HttpServletRequest request) {

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}