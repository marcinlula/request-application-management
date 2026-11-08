package com.marcinlula.requestmanagement.exception;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(UUID id) {
        super("Request " + id + " not found");
    }
}
