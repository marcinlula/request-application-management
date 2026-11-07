package com.marcinlula.requestmanagement.model;

import lombok.With;

import java.util.UUID;

@With
public record Request(UUID id, String name, String body, RequestState state, String reason, Long publicationNumber) {

    public static Request create(String name, String body) {
        UUID id = createId();
        return new Request(id, name, body, RequestState.CREATED, null, null);
    }

    private static UUID createId() {
        return UUID.randomUUID();
    }

}
