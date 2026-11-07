package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class RequestRepositoryImpl implements RequestRepository {

    private final RequestJpaRepository jpaRepository;
    private final RequestEntityMapper mapper;

    @Override
    public Request save(Request request) {
        RequestEntity entity = jpaRepository.findByUuid(request.id()).orElseGet(RequestEntity::new);
        mapper.updateEntity(request, entity);
        RequestEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Request> findById(UUID id) {
        return jpaRepository.findByUuid(id).map(mapper::toDomain);
    }
}
