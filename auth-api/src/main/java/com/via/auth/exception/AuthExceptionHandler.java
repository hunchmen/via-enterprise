package com.via.auth.exception;

import com.via.auth.dto.AuthErrorResponse;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthenticationException() {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(IncorrectCurrentPasswordException.class)
    public ResponseEntity<AuthErrorResponse> handleIncorrectCurrentPassword(
            IncorrectCurrentPasswordException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Optional<FieldError> firstError =
                exception.getBindingResult().getFieldErrors().stream().findFirst();
        String message = firstError
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<AuthErrorResponse> error(HttpStatus status, String message) {
        AuthErrorResponse body = new AuthErrorResponse(String.valueOf(status.value()), message);
        return ResponseEntity.status(status).body(body);
    }
}
