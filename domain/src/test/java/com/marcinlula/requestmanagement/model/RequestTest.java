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
