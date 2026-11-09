package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.RequestState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

interface RequestRepository {

    Request save(Request request);

    Optional<Request> findById(UUID id);

    Page<Request> search(String name, RequestState state, Pageable pageable);
}
