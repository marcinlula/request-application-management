package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.RequestState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "request_state_changes")
@Getter
@Setter
class StateChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id")
    private RequestEntity request;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 20)
    private RequestState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 20)
    private RequestState toState;

    @Column(length = 2_000)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant at;
}
