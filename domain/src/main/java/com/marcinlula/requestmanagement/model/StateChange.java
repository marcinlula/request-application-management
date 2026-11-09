package com.marcinlula.requestmanagement.model;

import java.time.Instant;

public record StateChange(RequestState from, RequestState to, String reason, Instant at) {
}
