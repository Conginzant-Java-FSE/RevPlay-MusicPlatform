package com.revplay.revplay.exception;

public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, "CONFLICT");
    }
}
