package com.marcinlula.requestmanagement.api.dto;

import com.marcinlula.requestmanagement.model.RequestState;

import java.time.Instant;

public record StateChangeDto(RequestState from, RequestState to, String reason, Instant at) {
}
