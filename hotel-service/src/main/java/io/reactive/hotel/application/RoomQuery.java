package io.reactive.hotel.application;

import reactor.core.publisher.Mono;

public interface RoomQuery {

    Mono<Boolean> isRoomAvailable(Integer hotelId, Integer roomId);

}
