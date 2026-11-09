package com.marcinlula.requestmanagement.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum RequestState {
    CREATED, VERIFIED, ACCEPTED, PUBLISHED, DELETED, REJECTED;

    public Set<RequestState> allowedTargets() {
        return switch (this) {
            case CREATED -> Set.of(VERIFIED, DELETED);
            case VERIFIED -> Set.of(ACCEPTED, REJECTED);
            case ACCEPTED -> Set.of(PUBLISHED, REJECTED);
            default -> Set.of();
        };
    }

    public boolean allowedBodyChange() {
        return switch (this) {
            case CREATED, VERIFIED -> true;
            default -> false;
        };
    }

}
