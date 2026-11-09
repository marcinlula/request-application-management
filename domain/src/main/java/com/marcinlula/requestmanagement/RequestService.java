package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.exception.InvalidRequestDataException;
import com.marcinlula.requestmanagement.exception.InvalidRequestStateException;
import com.marcinlula.requestmanagement.exception.RequestNotFoundException;
import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.RequestState;
import com.marcinlula.requestmanagement.model.StateChange;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.marcinlula.requestmanagement.model.RequestState.*;

@Service
public class RequestService {

    private final RequestRepository repository;
    private final PublicationNumberGenerator numberGenerator;

    public RequestService(RequestRepository repository, PublicationNumberGenerator numberGenerator) {
        this.repository = repository;
        this.numberGenerator = numberGenerator;
    }

    @Transactional
    public Request create(String name, String body) {
        require(name, "Name is required");
        require(body, "Body is required");
        return repository.save(Request.create(name, body));
    }

    @Transactional
    public Request changeBody(UUID id, String body) {
        Request request = get(id);
        require(body, "Body is required");
        if (!request.state().allowedBodyChange()) {
            throw new InvalidRequestStateException("Body can not be modified in " + request.state() + " state");
        }
        return repository.save(request.withBody(body));
    }

    @Transactional
    public Request verify(UUID id) {
        return transition(id, VERIFIED, null);
    }

    @Transactional
    public Request accept(UUID id) {
        return transition(id, ACCEPTED, null);
    }

    @Transactional
    public Request reject(UUID id, String reason) {
        require(reason, "Reason is required when rejecting a request");
        return transition(id, REJECTED, reason.trim());
    }

    @Transactional
    public Request delete(UUID id, String reason) {
        require(reason, "Reason is required when deleting a request");
        return transition(id, DELETED, reason.trim());
    }

    @Transactional
    public Request publish(UUID id) {
        Request request = get(id);
        checkTransition(request, PUBLISHED);
        long publicationNumber = numberGenerator.next();
        return repository.save(request.withPublicationNumber(publicationNumber).changeState(PUBLISHED, null));
    }

    @Transactional(readOnly = true)
    public Request get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<StateChange> getHistory(UUID id) {
        return get(id).history();
    }

    private Request transition(UUID id, RequestState target, String reason) {
        Request request = get(id);
        checkTransition(request, target);
        return repository.save(request.changeState(target, reason));
    }

    private void checkTransition(Request request, RequestState target) {
        if (!request.state().allowedTargets().contains(target)) {
            throw new InvalidRequestStateException("Transition from " + request.state() + " to " + target + " is not allowed");
        }
    }

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestDataException(message);
        }
    }
}
