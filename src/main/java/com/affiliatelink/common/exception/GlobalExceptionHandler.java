
package com.affiliatelink.common.exception;

import com.affiliatelink.common.exception.offre.CampagneNotValidateException;
import com.affiliatelink.common.exception.offre.OffreNotValideException;
import com.affiliatelink.common.exception.offre.StatusNotActiveException;
import com.affiliatelink.common.exception.offre.TagInvalideException;

import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.EmailRequiredException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.common.exception.profil.StatusCompteExcetion;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return customizedResponse(
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );
    }


    @ExceptionHandler(ProfileNotFoudException.class)
    public ResponseEntity<Map<String, String>> handleProfileNotFound(
            ProfileNotFoudException exception
    ) {
        return customizedResponse(
                "PROFILE_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(EmailRequiredException.class)
    public ResponseEntity<Map<String, String>> handleEmailRequired(
            EmailRequiredException exception
    ) {
        return customizedResponse(
                "EMAIL_REQUIRED",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(StatusCompteExcetion.class)
    public ResponseEntity<Map<String, String>> handleProfileStatus(
            StatusCompteExcetion exception
    ) {
        return customizedResponse(
                "PROFILE_STATUS_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(OffreNotValideException.class)
    public ResponseEntity<Map<String, String>> handleOffreValidation(
            OffreNotValideException exception
    ) {
        return customizedResponse(
                "OFFRE_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(TagInvalideException.class)
    public ResponseEntity<Map<String, String>> handleTagValidation(
            TagInvalideException exception
    ) {
        return customizedResponse(
                "TAG_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(CampagneNotValidateException.class)
    public ResponseEntity<Map<String, String>> handleCampagneValidation(
            CampagneNotValidateException exception
    ) {
        return customizedResponse(
                "CAMPAGNE_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(StatusNotActiveException.class)
    public ResponseEntity<Map<String, String>> handleStatusNotActive(
            StatusNotActiveException exception
    ) {
        return customizedResponse(
                "ADVERTISER_NOT_ACTIVE",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleDtoValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        errors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("error", "VALIDATION_ERROR");
        body.put("message", "Les données de la requête sont invalides");
        body.put("fields", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return customizedResponse(
                "INVALID_JSON",
                "Le JSON est invalide ou contient une valeur incompatible",
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            DataIntegrityViolationException exception
    ) {
        return customizedResponse(
                "DATA_INTEGRITY_VIOLATION",
                "L'opération viole une contrainte de la base de données",
                HttpStatus.CONFLICT
        );
    }


    private ResponseEntity<Map<String, String>> customizedResponse(
            String error,
            String message,
            HttpStatus status
    ) {

        Map<String, String> body = Map.of(
                "error", error,
                "message", message != null ? message : "Erreur métier"
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}
