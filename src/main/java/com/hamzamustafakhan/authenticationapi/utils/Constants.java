package com.hamzamustafakhan.authenticationapi.utils;

public class Constants {
//    USER/PASSWORD CONSTANTS
    public static final String INVALID_CREDENTIALS = "Invalid credentials entered";
    public static final String USER_EXISTS = "User already exists";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_DISABLED = "Account is currently inactive, kindly submit reset request or kindly approve if already submitted";
    public static final String INVALID_CREDENTIALS_RESET = "Account is made inactive after failed attempts, kindly submit reset request";
    public static final String REQUEST_EXISTS = "Request already exists";
    public static final String REQUEST_NOT_FOUND = "Request not found";
    public static final String SUBMIT_RESET_REQUEST = "Kindly submit request for password reset first";
    public static final String REQUEST_ALREADY_APPROVED = "Your reset request has already been approved";
    public static final String RESET_PASSWORD = "Kindly reset your password first as your reset request was approved";

//    OPERATION CONSTANTS
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String DB_EXCEPTION = "Error occurred in performing operation";


//    STATUS CONSTANTS
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
}
