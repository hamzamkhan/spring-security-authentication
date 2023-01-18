package com.hamzamustafakhan.authenticationapi.exception;

import com.hamzamustafakhan.authenticationapi.domain.ExceptionResponse;
import com.hamzamustafakhan.authenticationapi.domain.GenericResponse;
import com.hamzamustafakhan.authenticationapi.domain.ValidationErrorResponse;
import com.hamzamustafakhan.authenticationapi.utils.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({Exception.class, ServiceLayerException.class})
    public final ResponseEntity<GenericResponse> handleExceptions(ServiceLayerException ex, WebRequest request){
        List<String> details = new ArrayList<>();
        GenericResponse<ExceptionResponse> genericResponse = new GenericResponse<>();
        details.add(ex.getLocalizedMessage());
        String errorMessage = details.stream().map(event -> event).collect(Collectors.joining(","));
        ExceptionResponse response = new ExceptionResponse(ex.getHttpStatus().value(), errorMessage, request.getDescription(false));
        genericResponse.setResponse(response);
        genericResponse.setStatus(Constants.FAILED);
        return new ResponseEntity<>(genericResponse, ex.getHttpStatus());
    }



    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = ex.getFieldErrors().stream().map(fieldError -> fieldError.getField() + " : " + fieldError.getDefaultMessage()).collect(Collectors.toList());
        ValidationErrorResponse errorResponse = new ValidationErrorResponse("Validation failed", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
