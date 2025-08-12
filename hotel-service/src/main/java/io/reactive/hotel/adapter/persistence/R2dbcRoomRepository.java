package io.reactive.hotel.adapter.persistence;

import io.reactive.hotel.domain.Room;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

interface R2dbcRoomRepository extends R2dbcRepository<Room, Integer> {

    @Query("""
        SELECT available
        FROM room
        WHERE id = :roomId AND hotel_id = :hotelId
    """)
    Mono<Boolean> isRoomAvailable(Integer hotelId, Integer roomId);
}
