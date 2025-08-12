package io.reactive.hotel.domain;

import reactor.core.publisher.Mono;

public interface Hotels {

    Mono<Boolean> isRoomAvailable(Integer hotelId, Integer roomId);

}
