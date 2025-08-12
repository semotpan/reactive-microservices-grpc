package io.reactive.reservation.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface Reservations {

    Mono<Reservation> add(Reservation reservation);

    Mono<Reservation> update(Reservation reservation);

    Mono<Reservation> find(UUID id);

    Flux<Reservation> findAll();

    Mono<Boolean> hasConflict(Reservation reservation);
}
