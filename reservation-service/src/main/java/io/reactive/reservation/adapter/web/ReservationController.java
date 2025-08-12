package io.reactive.reservation.adapter.web;

import io.reactive.reservation.adapter.web.errorhandling.ApiErrorResponse;
import io.reactive.reservation.application.ReservationService;
import io.reactive.reservation.domain.Reservation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static io.reactive.reservation.adapter.web.errorhandling.ApiErrorResponse.notFound;
import static io.reactive.reservation.application.ReservationService.CreateReservationCommand;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
final class ReservationController {

    private final ReservationService reservationService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Reservation>> create(@Valid @RequestBody Mono<CreateReservationResource> resourceMono,
                                                    ServerHttpRequest request) {
        return resourceMono
                .map(CreateReservationResource::toCommand)
                .flatMap(reservationService::create)
                .map(reservation -> {
                    var location = ForwardedHeaderUtils
                            .adaptFromForwardedHeaders(request.getURI(), request.getHeaders())
                            .path("/{id}")
                            .buildAndExpand(reservation.getId())
                            .toUri();
                    return created(location).body(reservation);
                });
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Mono<Reservation> find(@PathVariable UUID id) {
        return reservationService.find(id);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Flux<Reservation> list() {
        return reservationService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<ApiErrorResponse>> cancel(@PathVariable UUID id) {
        return reservationService.cancel(id)
                .flatMap(success ->
                        success ? Mono.just(noContent().build())
                                : Mono.just(notFound("Reservation id '" + id + "' not found")));
    }

    @ValidReservationPeriod
    public record CreateReservationResource(@NotNull(message = "Hotel ID must be provided")
                                            @Positive(message = "Hotel ID must be a positive number")
                                            Integer hotelId,

                                            @NotNull(message = "Room ID must be provided")
                                            @Positive(message = "Room ID must be a positive number")
                                            Integer roomId,

                                            @NotNull(message = "Start date must be provided")
                                            @FutureOrPresent(message = "Start date cannot be in the past")
                                            LocalDate startDate,

                                            @NotNull(message = "End date must be provided")
                                            @FutureOrPresent(message = "End date cannot be in the past")
                                            LocalDate endDate,

                                            @NotNull(message = "Guest ID must be provided")
                                            @Positive(message = "Guest ID must be a positive number")
                                            Integer guestId) {

        CreateReservationCommand toCommand() {
            return new CreateReservationCommand(hotelId, roomId, startDate, endDate, guestId);
        }
    }
}
