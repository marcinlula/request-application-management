package com.marcinlula.requestmanagement.api;

import com.marcinlula.requestmanagement.exception.InvalidRequestDataException;
import com.marcinlula.requestmanagement.exception.InvalidRequestStateException;
import com.marcinlula.requestmanagement.exception.RequestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RequestNotFoundException.class)
    ProblemDetail notFound(RequestNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidRequestDataException.class)
    ProblemDetail badRequest(InvalidRequestDataException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidRequestStateException.class)
    ProblemDetail conflict(InvalidRequestStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
