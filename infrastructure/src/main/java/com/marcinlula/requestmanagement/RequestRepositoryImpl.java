package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.RequestState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        appendNewStateChanges(request, entity);
        RequestEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<Request> search(String name, RequestState state, Pageable pageable) {
        RequestEntity probe = new RequestEntity();
        probe.setName(name);
        probe.setState(state);
        probe.setHistory(null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", match -> match.contains().ignoreCase());
        return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
    }

    // The history is append-only: only add the entries that are not stored yet.
    private void appendNewStateChanges(Request request, RequestEntity entity) {
        for (int i = entity.getHistory().size(); i < request.history().size(); i++) {
            StateChangeEntity row = mapper.toEntity(request.history().get(i));
            row.setRequest(entity);
            entity.getHistory().add(row);
        }
    }

    @Override
    public Optional<Request> findById(UUID id) {
        return jpaRepository.findByUuid(id).map(mapper::toDomain);
    }
}
