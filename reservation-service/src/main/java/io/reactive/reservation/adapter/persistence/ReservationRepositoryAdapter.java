package io.reactive.reservation.adapter.persistence;

import io.reactive.reservation.domain.Reservation;
import io.reactive.reservation.domain.Reservations;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static io.reactive.reservation.domain.Reservation.Status.*;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
class ReservationRepositoryAdapter implements Reservations {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final ReservationRepository reservationRepository;

    @Override
    public Mono<Reservation> add(Reservation reservation) {
        requireNonNull(reservation, "reservation must not be null");
        return r2dbcEntityTemplate.insert(reservation);
    }

    @Override
    public Mono<Reservation> update(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public Mono<Reservation> find(UUID id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Flux<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Mono<Boolean> hasConflict(Reservation reservation) {
        return reservationRepository.hasConflict(reservation.getHotelId(), reservation.getRoomId(),
                reservation.getStartDate(), reservation.getEndDate(), reservation.getId(), Set.of(FAILED, CANCELED));
    }
}
