package io.reactive.reservation.application;

import io.reactive.reservation.domain.Reservation;
import io.reactive.reservation.domain.Reservations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static io.reactive.reservation.application.ReservationService.CreateReservationCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DefaultReservationServiceTest {

    @Mock
    HotelClient hotelClient;

    @Mock
    Reservations reservations;

    @InjectMocks
    DefaultReservationService service;

    CreateReservationCommand cmd = new CreateReservationCommand(
            1, 2, LocalDate.now(), LocalDate.now().plusDays(2), 1
    );

    @Test
    @DisplayName("Create reservation - should succeed when no conflict and room is available")
    void createShouldSucceedWhenNoConflictAndRoomIsAvailable() {
        // given
        var reservation = newReservation();

        when(reservations.add(any())).thenReturn(Mono.just(reservation));
        when(reservations.hasConflict(reservation)).thenReturn(Mono.just(false));
        when(hotelClient.checkRoomAvailable(cmd.hotelId(), cmd.roomId())).thenReturn(Mono.just(true));
        when(reservations.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // when & then
        StepVerifier.create(service.create(cmd))
                .expectNextMatches(r -> r.getStatus() == Reservation.Status.SUCCEED)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create reservation - should fail when time slot is already booked")
    void createShouldFailWhenConflictDetected() {
        // given
        var reservation = newReservation();

        when(reservations.add(any())).thenReturn(Mono.just(reservation));
        when(reservations.hasConflict(reservation)).thenReturn(Mono.just(true));
        when(reservations.update(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            // then
            assertEquals(Reservation.Status.FAILED, r.getStatus());
            return Mono.just(r);
        });

        // when & then
        StepVerifier.create(service.create(cmd))
                .expectNextMatches(r -> r.getStatus() == Reservation.Status.FAILED)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create reservation - should fail when room is not available in hotel")
    void createShouldFailWhenRoomIsUnavailable() {
        // given
        var reservation = newReservation();

        when(reservations.add(any())).thenReturn(Mono.just(reservation));
        when(reservations.hasConflict(reservation)).thenReturn(Mono.just(false));
        when(hotelClient.checkRoomAvailable(cmd.hotelId(), cmd.roomId())).thenReturn(Mono.just(false));
        when(reservations.update(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            // then
            assertEquals(Reservation.Status.FAILED, r.getStatus());
            return Mono.just(r);
        });

        // when & then
        StepVerifier.create(service.create(cmd))
                .expectNextMatches(r -> r.getStatus() == Reservation.Status.FAILED)
                .verifyComplete();
    }

    @Test
    @DisplayName("Find reservation - should return reservation when found by ID")
    void findShouldReturnReservationIfFound() {
        // given
        var reservation = newReservation();
        when(reservations.find(reservation.getId())).thenReturn(Mono.just(reservation));

        // when & then
        StepVerifier.create(service.find(reservation.getId()))
                .expectNext(reservation)
                .verifyComplete();
    }

    @Test
    @DisplayName("List reservations - should return all reservations")
    void listShouldReturnAllReservations() {
        // given
        var r1 = newReservation();
        var r2 = newReservation();
        when(reservations.findAll()).thenReturn(Flux.just(r1, r2));

        // when & then
        StepVerifier.create(service.list())
                .expectNext(r1, r2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Cancel reservation - should return true and mark as cancelled if reservation exists")
    void cancelShouldReturnTrueIfReservationExists() {
        // given
        var reservation = newReservation();

        when(reservations.find(reservation.getId())).thenReturn(Mono.just(reservation));
        when(reservations.update(any())).thenReturn(Mono.just(reservation));

        // when & then
        StepVerifier.create(service.cancel(reservation.getId()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Cancel reservation - should return false if reservation does not exist")
    void cancelShouldReturnFalseIfReservationNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(reservations.find(id)).thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(service.cancel(id))
                .expectNext(false)
                .verifyComplete();
    }

    private Reservation newReservation() {
        return Reservation.builder()
                .guestId(cmd.guestId())
                .hotelId(cmd.hotelId())
                .roomId(cmd.roomId())
                .startDate(cmd.startDate())
                .endDate(cmd.endDate())
                .build();
    }
}
