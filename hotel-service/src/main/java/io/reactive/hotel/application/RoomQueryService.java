package io.reactive.hotel.application;

import io.reactive.hotel.domain.Hotels;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class RoomQueryService implements RoomQuery {

    private final Hotels hotels;

    @Override
    public Mono<Boolean> isRoomAvailable(Integer hotelId, Integer roomId) {
        return hotels.isRoomAvailable(hotelId, roomId);
    }
}
