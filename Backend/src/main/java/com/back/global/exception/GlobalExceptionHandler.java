package com.back.global.exception;

import com.back.global.rsData.RsData;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        log.error("NoSuchElementException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new RsData<>("404", "The requested data does not exist."),
                NOT_FOUND
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<RsData<Void>> handle(UsernameNotFoundException ex) {
        log.error("UsernameNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new RsData<>("404", "User not found."),
                NOT_FOUND
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RsData<Void>> handle(BadCredentialsException ex) {
        log.error("BadCredentialsException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new RsData<>("401", "Incorrect username or password."),
                UNAUTHORIZED
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handle(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String[] pathParts = violation.getPropertyPath().toString().split("\\.", 2);
                    String field = pathParts.length > 1 ? pathParts[1] : violation.getPropertyPath().toString();
                    String[] messageTemplateBits = violation.getMessageTemplate()
                            .split("\\.");
                    String code = messageTemplateBits.length >= 2
                            ? messageTemplateBits[messageTemplateBits.length - 2]
                            : "Unknown";
                    String _message = violation.getMessage();

                    return "%s-%s-%s".formatted(field, code, _message);
                })
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        log.error("ConstraintViolationException: {}", message);
        return new ResponseEntity<>(
                new RsData<>("400", message),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        log.error("MethodArgumentNotValidException: {}", message);
        return new ResponseEntity<>(
                new RsData<>("400", message),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new RsData<>("400", "Request body is not valid."),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RsData<Void>> handle(MissingRequestHeaderException ex) {
        log.error("MissingRequestHeaderException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new RsData<>(
                        "400",
                        "%s-%s-%s".formatted(
                                ex.getHeaderName(),
                                "NotBlank",
                                ex.getLocalizedMessage()
                        )
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<RsData<Void>> handle(ServiceException ex) {
        log.error("ServiceException: {}", ex.getMessage());
        RsData<Void> rsData = ex.getRsData();

        return new ResponseEntity<>(
                rsData,
                ResponseEntity
                        .status(rsData.statusCode())
                        .build()
                        .getStatusCode()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handle(Exception ex) {
        log.error("Unexpected exception: ", ex);
        return new ResponseEntity<>(
                new RsData<>("500", "An internal server error occurred."),
                INTERNAL_SERVER_ERROR
        );
    }
}