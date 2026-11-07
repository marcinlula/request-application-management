package com.marcinlula.requestmanagement;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SequencePublicationNumberGenerator implements PublicationNumberGenerator {

    private static final String PUBLICATION_NUMBER_SEQ = "publication_number_seq";

    private final EntityManager entityManager;

    @Override
    public long next() {
        Number publicationNumber = (Number) entityManager
                .createNativeQuery("select next value for " + PUBLICATION_NUMBER_SEQ)
                .getSingleResult();
        return publicationNumber.longValue();
    }
}
