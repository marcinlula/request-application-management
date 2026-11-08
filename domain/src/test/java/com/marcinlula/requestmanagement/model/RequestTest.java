package com.marcinlula.requestmanagement.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTest {

    @Test
    void whenCreate_thenRequestIsInCreatedStateWithGeneratedId() {
        //given
        //when
        Request request = Request.create("name", "body");

        //then
        assertThat(request.id()).isNotNull();
        assertThat(request.name()).isEqualTo("name");
        assertThat(request.body()).isEqualTo("body");
        assertThat(request.state()).isEqualTo(RequestState.CREATED);
        assertThat(request.reason()).isNull();
        assertThat(request.publicationNumber()).isNull();
    }

    @Test
    void whenCreate_thenHistoryContainsInitialStateChange() {
        //given
        //when
        Request request = Request.create("name", "body");

        //then
        assertThat(request.history()).hasSize(1);
        StateChange first = request.history().getFirst();
        assertThat(first.from()).isNull();
        assertThat(first.to()).isEqualTo(RequestState.CREATED);
        assertThat(first.reason()).isNull();
        assertThat(first.at()).isNotNull();
    }

    @Test
    void whenChangeState_thenStateReasonAndHistoryAreUpdatedAndOriginalIsIntact() {
        //given
        Request original = Request.create("name", "body");

        //when
        Request changed = original.changeState(RequestState.DELETED, "duplicate");

        //then
        assertThat(changed.state()).isEqualTo(RequestState.DELETED);
        assertThat(changed.reason()).isEqualTo("duplicate");
        assertThat(changed.history()).hasSize(2);
        assertThat(changed.history().getLast())
                .extracting(StateChange::from, StateChange::to, StateChange::reason)
                .containsExactly(RequestState.CREATED, RequestState.DELETED, "duplicate");
        assertThat(original.state()).isEqualTo(RequestState.CREATED);
        assertThat(original.history()).hasSize(1);
    }

    @Test
    void whenCreateTwice_thenIdsAreDistinct() {
        //given
        //when
        Request first = Request.create("a", "b");
        Request second = Request.create("a", "b");

        //then
        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void whenUsingWithMethods_thenModifiedCopyIsReturnedAndOriginalIsIntact() {
        //given
        Request original = Request.create("name", "body");

        //when
        Request changed = original.withBody("new").withState(RequestState.VERIFIED);

        //then
        assertThat(changed.body()).isEqualTo("new");
        assertThat(changed.state()).isEqualTo(RequestState.VERIFIED);
        assertThat(changed.id()).isEqualTo(original.id());
        assertThat(original.body()).isEqualTo("body");
        assertThat(original.state()).isEqualTo(RequestState.CREATED);
    }
}
