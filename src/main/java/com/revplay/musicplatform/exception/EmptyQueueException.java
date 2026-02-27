package com.revplay.musicplatform.exception;

public class EmptyQueueException extends BaseException {

    public EmptyQueueException(String message) {
        super(message, "EMPTY_QUEUE");
    }
}

