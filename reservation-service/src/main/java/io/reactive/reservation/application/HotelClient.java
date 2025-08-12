package io.reactive.reservation.application;

import reactor.core.publisher.Mono;

public interface HotelClient {

    Mono<Boolean> checkRoomAvailable(Integer hotelId, Integer roomId);

}
