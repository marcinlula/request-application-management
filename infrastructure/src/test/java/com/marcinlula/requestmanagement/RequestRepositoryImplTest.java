package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.StateChange;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private TestEntityManager entityManager;
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
    void whenSaveRequestWithStateChanges_thenHistoryIsPersistedInOrder() {
        //given
        Request request = Request.create("name", "body");
        Request verified = request.changeState(VERIFIED, null);

        //when
        repository.save(request);
        repository.save(verified);
        entityManager.flush();
        entityManager.clear();

        //then
        Request found = repository.findById(request.id()).orElseThrow();
        assertThat(found.history()).isEqualTo(verified.history());
        assertThat(found.history()).extracting(StateChange::to).containsExactly(CREATED, VERIFIED);
    }

    @Test
    void whenSaveAgainWithMoreStateChanges_thenOnlyNewEntriesAreAppended() {
        //given
        Request request = repository.save(Request.create("name", "body"));
        Request rejected = request.changeState(VERIFIED, null).changeState(REJECTED, "bad");

        //when
        repository.save(rejected);
        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(entityManager.getEntityManager()
                .createQuery("select count(c) from StateChangeEntity c", Long.class).getSingleResult()).isEqualTo(3);
        assertThat(repository.findById(request.id()).orElseThrow().history())
                .extracting(StateChange::to).containsExactly(CREATED, VERIFIED, REJECTED);
    }

    @Test
    void whenSearchWithoutFilters_thenRequestsAreReturnedPaged() {
        //given
        for (int i = 1; i <= 5; i++) {
            repository.save(Request.create("req" + i, "body"));
        }

        //when
        var page = repository.search(null, null, PageRequest.of(1, 2, Sort.by("id")));

        //then
        assertThat(page.getContent()).extracting(Request::name).containsExactly("req3", "req4");
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void whenSearchByName_thenMatchIsCaseInsensitiveAndPartial() {
        //given
        repository.save(Request.create("Annual Report", "b"));
        repository.save(Request.create("report draft", "b"));
        repository.save(Request.create("Invoice", "b"));

        //when
        var page = repository.search("REPORT", null, PageRequest.of(0, 10, Sort.by("id")));

        //then
        assertThat(page.getContent()).extracting(Request::name).containsExactly("Annual Report", "report draft");
    }

    @Test
    void whenSearchByState_thenOnlyRequestsInThatStateAreReturned() {
        //given
        repository.save(Request.create("a", "b"));
        Request verified = repository.save(Request.create("c", "d").changeState(VERIFIED, null));

        //when
        var page = repository.search(null, VERIFIED, PageRequest.of(0, 10));

        //then
        assertThat(page.getContent()).extracting(Request::id).containsExactly(verified.id());
    }

    @Test
    void whenSearchByNameAndState_thenBothFiltersApply() {
        //given
        repository.save(Request.create("report", "b"));
        Request match = repository.save(Request.create("report 2", "b").changeState(VERIFIED, null));
        repository.save(Request.create("other", "b").changeState(VERIFIED, null));

        //when
        var page = repository.search("report", VERIFIED, PageRequest.of(0, 10));

        //then
        assertThat(page.getContent()).extracting(Request::id).containsExactly(match.id());
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
