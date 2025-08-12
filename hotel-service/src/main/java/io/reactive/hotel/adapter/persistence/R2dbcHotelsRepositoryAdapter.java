package io.reactive.hotel.adapter.persistence;

import io.reactive.hotel.domain.Hotels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
class R2dbcHotelsRepositoryAdapter implements Hotels {

    private final R2dbcRoomRepository r2DbcRoomRepository;

    @Override
    public Mono<Boolean> isRoomAvailable(Integer hotelId, Integer roomId) {
        return r2DbcRoomRepository.isRoomAvailable(hotelId, roomId);
    }
}
