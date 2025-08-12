package io.reactive.reservation.application;

import io.reactive.reservation.domain.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface ReservationService {

    Mono<Reservation> create(CreateReservationCommand cmd);

    Mono<Reservation> find(UUID id);

    Flux<Reservation> list();

    Mono<Boolean> cancel(UUID id);

    record CreateReservationCommand(Integer hotelId,
                                    Integer roomId,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    Integer guestId) {
    }
}
