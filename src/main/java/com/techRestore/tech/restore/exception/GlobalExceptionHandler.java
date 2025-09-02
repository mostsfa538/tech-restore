package com.techRestore.tech.restore.exception;

import com.techRestore.tech.restore.dto.common.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed: " + errorMessage,
                "VAL_001");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RUNTIME_ERROR",
                ex.getMessage(),
                "RT_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ActivationException.class)
    public ResponseEntity<ErrorResponse> handleActivationException(ActivationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "ACCOUNT_NOT_ACTIVATED",
                ex.getMessage(),
                "ACT_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_CREDENTIALS",
                "Invalid username or password",
                "AUTH_002");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RESPONSE_STATUS_ERROR",
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                "RSE_001");
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RESPONSE_STATUS_ERROR",
                ex.getMessage(),
                "RSE_001"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShopNotFoundException(NotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "SHOP_NOT_FOUND",
                ex.getMessage(),
                "SHOP_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "EMAIL_EXISTS",
                ex.getMessage(),
                "SHOP_002");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                "GEN_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "OTP_EXPIRED",
                ex.getMessage(),
                "OTP_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredOtpException.class)
    public ResponseEntity<ErrorResponse> handleExpiredOtp(ExpiredOtpException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "OTP_EXPIRED",
                ex.getMessage(),
                "OTP_001");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
