package com.affiliatelink.profile.common.exception;

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

    @ExceptionHandler(PublisherNotFoudException.class)
    public ResponseEntity<Map<String, String>> handlePublisherNotFound(PublisherNotFoudException exception
    ) {
        Map<String, String> body = Map.of(
                "error", "PUBLISHER NIT FOUND",
                "message", exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }


}