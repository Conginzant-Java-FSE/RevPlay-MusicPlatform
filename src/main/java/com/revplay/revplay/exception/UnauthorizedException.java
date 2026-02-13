package com.revplay.revplay.exception;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
