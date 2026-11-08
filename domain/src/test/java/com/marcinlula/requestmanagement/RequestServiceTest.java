package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.exception.InvalidRequestDataException;
import com.marcinlula.requestmanagement.exception.InvalidRequestStateException;
import com.marcinlula.requestmanagement.exception.RequestNotFoundException;
import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.RequestState;
import com.marcinlula.requestmanagement.model.StateChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.marcinlula.requestmanagement.model.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    @Mock
    private RequestRepository repository;

    @Mock
    private PublicationNumberGenerator numberGenerator;

    private RequestService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));
        service = new RequestService(repository, numberGenerator);
    }

    @Test
    void whenReject_thenStateChangeWithReasonIsAppendedToHistory() {
        //given
        Request request = getRequest(VERIFIED);

        //when
        Request rejected = service.reject(request.id(), " bad ");

        //then
        assertThat(rejected.history()).hasSize(request.history().size() + 1);
        StateChange last = rejected.history().getLast();
        assertThat(last.from()).isEqualTo(VERIFIED);
        assertThat(last.to()).isEqualTo(REJECTED);
        assertThat(last.reason()).isEqualTo("bad");
        assertThat(last.at()).isNotNull();
    }

    @Test
    void whenPublish_thenStateChangeIsAppendedToHistory() {
        //given
        Request request = getRequest(ACCEPTED);
        when(numberGenerator.next()).thenReturn(1L);

        //when
        Request published = service.publish(request.id());

        //then
        StateChange last = published.history().getLast();
        assertThat(last.from()).isEqualTo(ACCEPTED);
        assertThat(last.to()).isEqualTo(PUBLISHED);
    }

    @Test
    void whenChangeBody_thenHistoryIsNotChanged() {
        //given
        Request request = getRequest(CREATED);

        //when
        Request changed = service.changeBody(request.id(), "new");

        //then
        assertThat(changed.history()).isEqualTo(request.history());
    }

    @Test
    void whenTransitionFails_thenHistoryIsNotSaved() {
        //given
        Request request = getRequest(PUBLISHED);

        //when
        var thrown = assertThatThrownBy(() -> service.verify(request.id()));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void whenGetHistory_thenHistoryOfRequestIsReturned() {
        //given
        Request request = getRequest(CREATED);

        //when
        var history = service.getHistory(request.id());

        //then
        assertThat(history).isEqualTo(request.history());
    }

    @Test
    void whenGetHistoryOfUnknownRequest_thenRequestNotFoundExceptionIsThrown() {
        //given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        var thrown = assertThatThrownBy(() -> service.getHistory(id));

        //then
        thrown.isInstanceOf(RequestNotFoundException.class);
    }

    private Request getRequest(RequestState state) {
        Request request = Request.create("name", "body").withState(state);
        when(repository.findById(request.id())).thenReturn(Optional.of(request));
        return request;
    }

    @Test
    void whenCreate_thenNewRequestIsSavedInCreatedState() {
        //given
        //when
        Request created = service.create("name", "body");

        //then
        assertThat(created.state()).isEqualTo(CREATED);
        assertThat(created.name()).isEqualTo("name");
        assertThat(created.body()).isEqualTo("body");
        verify(repository).save(created);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void whenCreateWithBlankName_thenInvalidRequestDataExceptionIsThrown(String name) {
        //given
        //when
        var thrown = assertThatThrownBy(() -> service.create(name, "body"));

        //then
        thrown.isInstanceOf(InvalidRequestDataException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void whenCreateWithBlankBody_thenInvalidRequestDataExceptionIsThrown(String body) {
        //given
        //when
        var thrown = assertThatThrownBy(() -> service.create("name", body));

        //then
        thrown.isInstanceOf(InvalidRequestDataException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "VERIFIED"})
    void whenChangeBodyInCreatedOrVerified_thenBodyIsChangedAndStateKept(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        Request changed = service.changeBody(request.id(), "new body");

        //then
        assertThat(changed.body()).isEqualTo("new body");
        assertThat(changed.state()).isEqualTo(state);
        verify(repository).save(changed);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"ACCEPTED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenChangeBodyInOtherStates_thenInvalidRequestStateExceptionIsThrown(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.changeBody(request.id(), "new"));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void whenChangeBodyToBlank_thenInvalidRequestDataExceptionIsThrown() {
        //given
        Request request = getRequest(CREATED);

        //when
        var thrown = assertThatThrownBy(() -> service.changeBody(request.id(), " "));

        //then
        thrown.isInstanceOf(InvalidRequestDataException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void whenVerifyCreated_thenStateIsVerified() {
        //given
        Request request = getRequest(CREATED);

        //when
        Request verified = service.verify(request.id());

        //then
        assertThat(verified.state()).isEqualTo(VERIFIED);
    }

    @Test
    void whenAcceptVerified_thenStateIsAccepted() {
        //given
        Request request = getRequest(VERIFIED);

        //when
        Request accepted = service.accept(request.id());

        //then
        assertThat(accepted.state()).isEqualTo(ACCEPTED);
    }

    @Test
    void whenPublishAccepted_thenStateIsPublishedAndNumberIsAssigned() {
        //given
        Request request = getRequest(ACCEPTED);
        when(numberGenerator.next()).thenReturn(1L);

        //when
        Request published = service.publish(request.id());

        //then
        assertThat(published.state()).isEqualTo(PUBLISHED);
        assertThat(published.publicationNumber()).isEqualTo(1L);
    }

    @Test
    void whenRejectVerifiedOrAccepted_thenStateIsRejectedWithTrimmedReason() {
        //given
        Request verified = getRequest(VERIFIED);
        Request accepted = getRequest(ACCEPTED);

        //when
        Request first = service.reject(verified.id(), "  not ok ");
        Request second = service.reject(accepted.id(), "bad");

        //then
        assertThat(first.state()).isEqualTo(REJECTED);
        assertThat(first.reason()).isEqualTo("not ok");
        assertThat(second.state()).isEqualTo(REJECTED);
        assertThat(second.reason()).isEqualTo("bad");
    }

    @Test
    void whenDeleteCreated_thenStateIsDeletedWithReason() {
        //given
        Request request = getRequest(CREATED);

        //when
        Request deleted = service.delete(request.id(), "duplicate");

        //then
        assertThat(deleted.state()).isEqualTo(DELETED);
        assertThat(deleted.reason()).isEqualTo("duplicate");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void whenRejectOrDeleteWithBlankReason_thenInvalidRequestDataExceptionIsThrown(String reason) {
        //given
        UUID id = UUID.randomUUID();

        //when
        var rejectThrown = assertThatThrownBy(() -> service.reject(id, reason));
        var deleteThrown = assertThatThrownBy(() -> service.delete(id, reason));

        //then
        rejectThrown.isInstanceOf(InvalidRequestDataException.class);
        deleteThrown.isInstanceOf(InvalidRequestDataException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"VERIFIED", "ACCEPTED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenVerifyNotCreated_thenInvalidRequestStateExceptionIsThrown(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.verify(request.id()));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "ACCEPTED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenAcceptNotVerified_thenInvalidRequestStateExceptionIsThrown(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.accept(request.id()));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenRejectOutsideVerifiedAndAccepted_thenInvalidRequestStateExceptionIsThrown(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.reject(request.id(), "r"));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"VERIFIED", "ACCEPTED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenDeleteNotCreated_thenInvalidRequestStateExceptionIsThrown(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.delete(request.id(), "r"));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"CREATED", "VERIFIED", "PUBLISHED", "DELETED", "REJECTED"})
    void whenPublishNotAccepted_thenInvalidRequestStateExceptionIsThrownAndNumberIsNotConsumed(RequestState state) {
        //given
        Request request = getRequest(state);

        //when
        var thrown = assertThatThrownBy(() -> service.publish(request.id()));

        //then
        thrown.isInstanceOf(InvalidRequestStateException.class);
        verify(numberGenerator, never()).next();
        verify(repository, never()).save(any());
    }

    @Test
    void whenGetExisting_thenRequestIsReturned() {
        //given
        Request request = getRequest(CREATED);

        //when
        Request found = service.get(request.id());

        //then
        assertThat(found).isEqualTo(request);
    }

    @Test
    void whenIdIsUnknown_thenRequestNotFoundExceptionIsThrown() {
        //given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        var getThrown = assertThatThrownBy(() -> service.get(id));
        var verifyThrown = assertThatThrownBy(() -> service.verify(id));
        var publishThrown = assertThatThrownBy(() -> service.publish(id));

        //then
        getThrown.isInstanceOf(RequestNotFoundException.class);
        verifyThrown.isInstanceOf(RequestNotFoundException.class);
        publishThrown.isInstanceOf(RequestNotFoundException.class);
    }
}
