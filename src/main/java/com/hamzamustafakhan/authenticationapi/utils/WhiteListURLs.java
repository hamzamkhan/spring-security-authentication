package com.hamzamustafakhan.authenticationapi.utils;

public interface WhiteListURLs {
    String[] WhiteList = {
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/h2-console/**",
            "/console/**",
            "/favicon.ico",
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/request-reset-password",
            "/api/auth/approve-request-reset-password/**",
            "/api/auth/reset-password"

    };
}
