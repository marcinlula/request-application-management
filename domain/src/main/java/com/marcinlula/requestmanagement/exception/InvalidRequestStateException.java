package com.marcinlula.requestmanagement.exception;

public class InvalidRequestStateException extends RuntimeException {

    public InvalidRequestStateException(String message) {
        super(message);
    }
}
