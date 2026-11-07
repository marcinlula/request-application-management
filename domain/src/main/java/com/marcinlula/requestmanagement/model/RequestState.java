package com.marcinlula.requestmanagement.model;

import java.util.Set;

public enum RequestState {
    CREATED, VERIFIED, ACCEPTED, PUBLISHED, DELETED, REJECTED;

    public Set<RequestState> allowedTargets() {
        return switch (this) {
            case CREATED -> Set.of(VERIFIED, DELETED);
            case VERIFIED -> Set.of(ACCEPTED, REJECTED);
            case ACCEPTED -> Set.of(PUBLISHED, REJECTED);
            case PUBLISHED, DELETED, REJECTED -> Set.of();
        };
    }
}
