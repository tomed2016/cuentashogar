package com.householdfinance.identity.adapters.in.rest;

import com.householdfinance.identity.domain.DomainException;
import com.householdfinance.identity.domain.Email;
import com.householdfinance.identity.domain.InactiveUserException;
import com.householdfinance.identity.domain.InvalidCredentialsException;
import com.householdfinance.identity.domain.InvalidRefreshTokenException;
import com.householdfinance.identity.domain.RawPassword;
import com.householdfinance.identity.domain.UserAlreadyExistsException;
import com.householdfinance.identity.domain.UserNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Manejador global de excepciones que traduce excepciones de dominio y de framework a
 * respuestas {@link ProblemDetail} conformes con RFC 9457, sin exponer detalles internos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI PROBLEM_BASE_TYPE = URI.create("https://household-finance.dev/problems/");

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Credenciales invalidas", request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(InvalidRefreshTokenException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token invalido o expirado", request);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ProblemDetail> handleInactiveUser(InactiveUserException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "USER_INACTIVE", ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(Email.InvalidEmailException.class)
    public ResponseEntity<ProblemDetail> handleInvalidEmail(Email.InvalidEmailException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_EMAIL", ex.getMessage(), request);
    }

    @ExceptionHandler(RawPassword.WeakPasswordException.class)
    public ResponseEntity<ProblemDetail> handleWeakPassword(RawPassword.WeakPasswordException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex, WebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "DOMAIN_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Error inesperado no controlado", ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Ha ocurrido un error inesperado. Intente nuevamente mas tarde.",
                request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ProblemDetail problemDetail = newProblemDetail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La peticion contiene datos invalidos", request);
        problemDetail.setProperty("validationErrors", errors);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail =
                newProblemDetail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La peticion contiene datos invalidos", request);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    private ResponseEntity<ProblemDetail> build(HttpStatus status, String errorCode, String detail, WebRequest request) {
        ProblemDetail problemDetail = newProblemDetail(status, errorCode, detail, request);
        return ResponseEntity.status(status).body(problemDetail);
    }

    private ProblemDetail newProblemDetail(HttpStatus status, String errorCode, String detail, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(PROBLEM_BASE_TYPE.resolve(errorCode.toLowerCase().replace('_', '-')));
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        return problemDetail;
    }

    /** Detalle de un error de validacion sobre un campo especifico del cuerpo de la peticion. */
    public record ValidationError(String field, String message) {}
}
