package com.hamzamustafakhan.authenticationapi.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ServiceLayerException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    public ServiceLayerException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }
}
