package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.RequestState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
@Setter
class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10_000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestState state;

    private String reason;

    @Column(unique = true)
    private Long publicationNumber;
}
