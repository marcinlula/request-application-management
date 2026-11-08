package com.marcinlula.requestmanagement.model;

import lombok.With;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@With
public record Request(UUID id, String name, String body, RequestState state, String reason, Long publicationNumber,
                      List<StateChange> history) {

    public Request {
        history = history == null ? List.of() : List.copyOf(history);
    }

    public static Request create(String name, String body) {
        UUID id = createId();
        List<StateChange> history = List.of(new StateChange(null, RequestState.CREATED, null, Instant.now().truncatedTo(ChronoUnit.MICROS)));
        return new Request(id, name, body, RequestState.CREATED, null, null, history);
    }

    public Request changeState(RequestState target, String reason) {
        List<StateChange> newHistory = new ArrayList<>(history);
        newHistory.add(new StateChange(state, target, reason, Instant.now().truncatedTo(ChronoUnit.MICROS)));
        return withState(target).withReason(reason).withHistory(newHistory);
    }

    private static UUID createId() {
        return UUID.randomUUID();
    }

}
