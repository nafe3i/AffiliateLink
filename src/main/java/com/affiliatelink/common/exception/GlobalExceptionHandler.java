
package com.affiliatelink.common.exception;

import com.affiliatelink.common.exception.offre.*;
import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.EmailRequiredException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.common.exception.profil.StatusCompteExcetion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =====================================
    // EXCEPTIONS DU MODULE PROFILE
    // =====================================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return rejected(
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(ProfileNotFoudException.class)
    public ResponseEntity<Map<String, String>> handleProfileNotFound(
            ProfileNotFoudException exception
    ) {
        return rejected(
                "PROFILE_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(EmailRequiredException.class)
    public ResponseEntity<Map<String, String>> handleEmailRequired(
            EmailRequiredException exception
    ) {
        return rejected(
                "EMAIL_REQUIRED",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(StatusCompteExcetion.class)
    public ResponseEntity<Map<String, String>> handleProfileStatus(
            StatusCompteExcetion exception
    ) {
        return rejected(
                "PROFILE_STATUS_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // =====================================
    // EXCEPTIONS DU MODULE OFFRE
    // =====================================

    @ExceptionHandler(OffreNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOffreNotFound(
            OffreNotFoundException exception
    ) {
        return rejected(
                "OFFRE_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(OffreNotValideException.class)
    public ResponseEntity<Map<String, String>> handleOffreValidation(
            OffreNotValideException exception
    ) {
        return rejected(
                "OFFRE_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(TagInvalideException.class)
    public ResponseEntity<Map<String, String>> handleTagValidation(
            TagInvalideException exception
    ) {
        return rejected(
                "TAG_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(StatusNotActiveException.class)
    public ResponseEntity<Map<String, String>> handleStatusNotActive(
            StatusNotActiveException exception
    ) {
        return rejected(
                "ADVERTISER_NOT_ACTIVE",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    // =====================================
    // EXCEPTIONS DU MODULE CAMPAGNE
    // =====================================

    @ExceptionHandler(CampagneNotValidateException.class)
    public ResponseEntity<Map<String, String>> handleCampagneValidation(
            CampagneNotValidateException exception
    ) {
        return rejected(
                "CAMPAGNE_INVALID",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CampagneNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCampagneNotFound(
            CampagneNotFoundException exception
    ) {
        return rejected(
                "CAMPAGNE_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(CampagneStatusConflictException.class)
    public ResponseEntity<Map<String, String>> handleCampagneStatusConflict(
            CampagneStatusConflictException exception
    ) {
        return rejected(
                "CAMPAGNE_STATUS_CONFLICT",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    // =====================================
    // VALIDATION DES DTO
    // =====================================

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
                                fieldError.getDefaultMessage() != null
                                        ? fieldError.getDefaultMessage()
                                        : "Valeur invalide"
                        )
                );

        log.warn(
                "HTTP request rejected status=400 error=VALIDATION_ERROR invalidFields={}",
                errors.keySet()
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put(
                "message",
                "Les données de la requête sont invalides"
        );
        body.put("fields", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    // =====================================
    // JSON INVALIDE OU TYPE INCOMPATIBLE
    // =====================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return rejected(
                "INVALID_JSON",
                "Le JSON est invalide ou contient une valeur incompatible",
                HttpStatus.BAD_REQUEST
        );
    }

    // =====================================
    // PARAMETRE DE REQUETE INVALIDE
    // =====================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return rejected(
                "INVALID_PARAMETER",
                "Type invalide pour le paramètre : "
                        + exception.getName(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(
            MissingServletRequestParameterException exception
    ) {
        return rejected(
                "MISSING_PARAMETER",
                "Paramètre obligatoire manquant : "
                        + exception.getParameterName(),
                HttpStatus.BAD_REQUEST
        );
    }

    // =====================================
    // ERREUR HTTP EXPLICITE
    // =====================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException exception
    ) {

        HttpStatusCode status = exception.getStatusCode();

        String message = exception.getReason() != null
                ? exception.getReason()
                : "La requête ne peut pas être traitée";

        if (status.is5xxServerError()) {

            log.error(
                    "HTTP operation failed status={} error=HTTP_ERROR",
                    status.value(),
                    exception
            );

        } else {

            log.warn(
                    "HTTP request rejected status={} error=HTTP_ERROR",
                    status.value()
            );
        }

        return response(
                "HTTP_ERROR",
                message,
                status
        );
    }

    // =====================================
    // VIOLATION DES CONTRAINTES SQL
    // =====================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            DataIntegrityViolationException exception
    ) {

        return rejected(
                "DATA_INTEGRITY_VIOLATION",
                "L'opération viole une contrainte de la base de données",
                HttpStatus.CONFLICT
        );
    }

    // =====================================
    // ERREUR INATTENDUE
    // =====================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(
            Exception exception
    ) {

        // Les exceptions HTTP de Spring possèdent déjà
        // un statut : ne pas les convertir en HTTP 500.
        if (exception instanceof ErrorResponse httpError) {

            HttpStatusCode status = httpError.getStatusCode();

            if (status.is5xxServerError()) {

                log.error(
                        "HTTP framework error status={} exceptionType={}",
                        status.value(),
                        exception.getClass().getSimpleName(),
                        exception
                );

            } else {

                log.warn(
                        "HTTP request rejected status={} exceptionType={}",
                        status.value(),
                        exception.getClass().getSimpleName()
                );
            }

            String message;

            if (status.value() == 404) {
                message = "Ressource introuvable";
            } else if (status.value() == 405) {
                message = "Méthode HTTP non autorisée";
            } else if (status.is5xxServerError()) {
                message = "Erreur interne du serveur";
            } else {
                message = "La requête ne peut pas être traitée";
            }

            return response(
                    "HTTP_ERROR",
                    message,
                    status
            );
        }

        // Ici, il s'agit d'une véritable exception
        // non prise en charge par les handlers précédents.
        log.error(
                "Unexpected application error exceptionType={}",
                exception.getClass().getSimpleName(),
                exception
        );

        return response(
                "INTERNAL_SERVER_ERROR",
                "Une erreur interne est survenue",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // =====================================
    // REPONSE POUR LES REFUS METIER
    // =====================================

    private ResponseEntity<Map<String, String>> rejected(
            String error,
            String message,
            HttpStatus status
    ) {

        log.warn(
                "HTTP request rejected status={} error={}",
                status.value(),
                error
        );

        return response(error, message, status);
    }

    // =====================================
    // CONSTRUCTION DE LA REPONSE HTTP
    // =====================================

    private ResponseEntity<Map<String, String>> response(
            String error,
            String message,
            HttpStatusCode status
    ) {

        Map<String, String> body = Map.of(
                "error", error,
                "message", message != null
                        ? message
                        : "Erreur lors du traitement"
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}
