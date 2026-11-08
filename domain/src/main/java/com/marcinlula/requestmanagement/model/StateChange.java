package com.marcinlula.requestmanagement.model;

import java.time.Instant;

/**
 * One audit-log entry. {@code from} is null for the initial creation.
 */
public record StateChange(RequestState from, RequestState to, String reason, Instant at) {
}
