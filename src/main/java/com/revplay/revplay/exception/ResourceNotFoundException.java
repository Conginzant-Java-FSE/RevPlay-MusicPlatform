package com.revplay.revplay.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
