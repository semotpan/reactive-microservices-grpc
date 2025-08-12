package io.reactive.reservation.adapter.persistence;

import io.reactive.reservation.domain.Reservation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

interface ReservationRepository extends R2dbcRepository<Reservation, UUID> {

    @Query("""
            SELECT COUNT(*) > 0
            FROM reservation
            WHERE hotel_id = :hotelId
              AND room_id = :roomId
              AND status NOT IN (:excludedStatuses)
              AND (start_date, end_date) OVERLAPS (:startDate, :endDate)
              AND id != :excludeId
            """)
    Mono<Boolean> hasConflict(Integer hotelId,
                              Integer roomId,
                              LocalDate startDate,
                              LocalDate endDate,
                              UUID excludeId,
                              Set<Reservation.Status> excludedStatuses);

}
