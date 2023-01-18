package com.hamzamustafakhan.authenticationapi.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericResponse<T> {
    T response;
    String status;
}
