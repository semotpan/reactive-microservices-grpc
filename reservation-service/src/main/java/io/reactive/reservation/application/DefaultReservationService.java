package io.reactive.reservation.application;

import io.reactive.reservation.domain.Reservation;
import io.reactive.reservation.domain.Reservations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class DefaultReservationService implements ReservationService {

    private final HotelClient hotelClient;
    private final Reservations reservations;

    @Override
    @Transactional
    public Mono<Reservation> create(CreateReservationCommand cmd) {
        var reservation = Reservation.builder()
                .guestId(cmd.guestId())
                .hotelId(cmd.hotelId())
                .roomId(cmd.roomId())
                .startDate(cmd.startDate())
                .endDate(cmd.endDate())
                .build();

        return reservations.add(reservation)
                .flatMap(saved -> reservations.hasConflict(saved)
                        .flatMap(conflict -> {
                            // TODO: throw a proper exception with proper status code
                            if (conflict) {
                                return failReservation(saved, "Time slot already booked");
                            }

                            return hotelClient.checkRoomAvailable(saved.getHotelId(), saved.getRoomId())
                                    .flatMap(available -> {
                                        if (!available) {
                                            return failReservation(saved, "Room not available");
                                        }

                                        saved.markSucceed();
                                        return reservations.update(saved);
                                    });
                        })
                );
    }

    private Mono<Reservation> failReservation(Reservation reservation, String reason) {
        reservation.markFailed();
        log.warn("Reservation {} failed: {}", reservation.getId(), reason);
        return reservations.update(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Reservation> find(UUID id) {
        return reservations.find(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Reservation> list() {
        return reservations.findAll();
    }

    @Override
    @Transactional
    public Mono<Boolean> cancel(UUID id) {
        return reservations.find(id)
                .flatMap(reservation -> {
                    reservation.markCancelled();
                    return reservations.update(reservation)
                            .thenReturn(true);
                })
                .defaultIfEmpty(false); // If not found, return false
    }
}
