package dev.emanon.banking.customer.api.error;

import dev.emanon.banking.customer.application.exception.CustomerEmailAlreadyExistsException;
import dev.emanon.banking.customer.application.exception.CustomerNotFoundException;
import dev.emanon.banking.customer.application.exception.CustomerVersionConflictException;
import dev.emanon.banking.customer.domain.exception.InvalidCustomerStatusTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiError> handleCustomerNotFound(
            CustomerNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(CustomerEmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCustomerEmailAlreadyExists(
            CustomerEmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ValidationApiError error = new ValidationApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(CustomerVersionConflictException.class)
    public ResponseEntity<ApiError> handleCustomerVersionConflict(
            CustomerVersionConflictException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Customer was modified by another request",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(InvalidCustomerStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidStatusTransition(
            InvalidCustomerStatusTransitionException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}