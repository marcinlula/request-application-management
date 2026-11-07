package com.marcinlula.requestmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface RequestJpaRepository extends JpaRepository<RequestEntity, Long> {

    Optional<RequestEntity> findByUuid(UUID uuid);
}
