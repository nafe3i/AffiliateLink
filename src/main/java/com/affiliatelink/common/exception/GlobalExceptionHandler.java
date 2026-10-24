package com.affiliatelink.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {

        Map<String, String> body = Map.of(
                "error", "EMAIL_ALREADY_EXISTS",
                "message", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(ProfileNotFoudException.class)
    public ResponseEntity<Map<String, String>> handlePublisherNotFound(ProfileNotFoudException exception
    ) {
        Map<String, String> body = Map.of(
                "error", "PROFIL NIT FOUND",
                "message", exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EmailRequiredException.class)
    public ResponseEntity<Map<String, String>> handleEmailRequired(EmailRequiredException exception) {
        Map<String, String> body = Map.of(
                "error", "EMAIL REQURED",
                "message", exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


}