package com.marcinlula.requestmanagement.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.marcinlula.requestmanagement.model.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;

class RequestStateTest {

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = {"PUBLISHED", "DELETED", "REJECTED"})
    void whenStateIsFinal_thenNoTransitionsAreAllowed(RequestState state) {
        //when
        var targets = state.allowedTargets();

        //then
        assertThat(targets).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("stateDiagram")
    void whenStateIsNotFinal_thenTransitionsMatchStateDiagram(RequestState from, RequestState[] expectedTargets) {
        //when
        var targets = from.allowedTargets();

        //then
        assertThat(targets).containsExactlyInAnyOrder(expectedTargets);
    }

    static Stream<Arguments> stateDiagram() {
        return Stream.of(
                Arguments.of(CREATED, new RequestState[]{VERIFIED, DELETED}),
                Arguments.of(VERIFIED, new RequestState[]{ACCEPTED, REJECTED}),
                Arguments.of(ACCEPTED, new RequestState[]{PUBLISHED, REJECTED})
        );
    }
}
