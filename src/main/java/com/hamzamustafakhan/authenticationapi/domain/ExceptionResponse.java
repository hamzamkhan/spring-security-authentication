package com.hamzamustafakhan.authenticationapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private int statusCode;
    private String message;
    private String description;
}
