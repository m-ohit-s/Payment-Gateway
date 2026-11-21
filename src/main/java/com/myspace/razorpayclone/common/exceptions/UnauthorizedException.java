package com.myspace.razorpayclone.common.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final String errorCode;
    private final String errorDescription;

    public UnauthorizedException(String errorCode, String errorDescription) {
        super("Unauthorized User: " + errorDescription);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
}
