package com.cao.flighttracker.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.cao.flighttracker.aircraft.AircraftAlreadyExistsException;
import com.cao.flighttracker.aircraft.AircraftNotFoundException;
import com.cao.flighttracker.aircraft.NoLivePositionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AircraftNotFoundException.class)
    public ProblemDetail handleNotFound(AircraftNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoLivePositionException.class)
    public ProblemDetail handleNoLivePosition(NoLivePositionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AircraftAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(AircraftAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AdminKeyException.class)
    public ProblemDetail handleAdminKey(AdminKeyException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Invalid request");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ProblemDetail handleOpenSkyQuota(HttpClientErrorException.TooManyRequests ex) {
        log.warn("OpenSky quota exhausted: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "OpenSky quota exhausted, try again later");
    }

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleOpenSkyFailure(RestClientException ex) {
        log.error("OpenSky call failed", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "OpenSky is currently unavailable");
    }
}