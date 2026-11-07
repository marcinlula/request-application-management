package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;

import java.util.Optional;
import java.util.UUID;

interface RequestRepository {

    Request save(Request request);

    Optional<Request> findById(UUID id);
}
