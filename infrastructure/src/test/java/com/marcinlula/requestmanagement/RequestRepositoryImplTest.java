package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static com.marcinlula.requestmanagement.model.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({RequestRepositoryImpl.class, RequestEntityMapperImpl.class, SequencePublicationNumberGenerator.class})
class RequestRepositoryImplTest {

    @Autowired
    private RequestRepositoryImpl repository;
    @Autowired
    private RequestJpaRepository jpaRepository;
    @Autowired
    private SequencePublicationNumberGenerator numberGenerator;

    @Test
    void whenSaveNewRequest_thenItIsFoundByDomainId() {
        //given
        Request request = Request.create("name", "body");

        //when
        Request saved = repository.save(request);

        //then
        assertThat(saved).isEqualTo(request);
        assertThat(repository.findById(request.id())).contains(request);
    }

    @Test
    void whenSaveExistingRequest_thenRowIsUpdatedNotDuplicated() {
        //given
        Request request = repository.save(Request.create("name", "body"));

        //when
        Request updated = repository.save(request.withBody("new").withState(REJECTED).withReason("bad"));

        //then
        assertThat(jpaRepository.count()).isEqualTo(1);
        assertThat(updated.body()).isEqualTo("new");
        assertThat(updated.state()).isEqualTo(REJECTED);
        assertThat(repository.findById(request.id())).contains(updated);
    }

    @Test
    void whenSavePublishedRequest_thenPublicationNumberIsPersisted() {
        //given
        Request request = repository.save(Request.create("name", "body"));

        //when
        repository.save(request.withState(PUBLISHED).withPublicationNumber(42L));

        //then
        assertThat(repository.findById(request.id()).orElseThrow().publicationNumber()).isEqualTo(42L);
    }

    @Test
    void whenFindUnknownId_thenEmptyIsReturned() {
        //given
        //when
        var found = repository.findById(UUID.randomUUID());

        //then
        assertThat(found).isEmpty();
    }

    @Test
    void whenTwoRequestsHaveSamePublicationNumber_thenConstraintViolationIsThrown() {
        //given
        Request first = repository.save(Request.create("a", "b"));
        Request second = repository.save(Request.create("c", "d"));
        repository.save(first.withState(PUBLISHED).withPublicationNumber(1L));

        //when
        var thrown = assertThatThrownBy(() -> {
            repository.save(second.withState(PUBLISHED).withPublicationNumber(1L));
            jpaRepository.flush();
        });

        //then
        thrown.isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void whenNextCalledRepeatedly_thenNumbersAreStrictlyIncreasing() {
        //given
        //when
        long first = numberGenerator.next();
        long second = numberGenerator.next();
        long third = numberGenerator.next();

        //then
        assertThat(second).isEqualTo(first + 1);
        assertThat(third).isEqualTo(second + 1);
    }
}
