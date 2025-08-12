package io.reactive.reservation.adapter.grpc;

import io.grpc.StatusRuntimeException;
import io.reactive.hotel.grpc.HotelServiceGrpc;
import io.reactive.hotel.grpc.RoomAvailabilityRequest;
import io.reactive.reservation.application.HotelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
final class GrpcHotelClient implements HotelClient {

    private final HotelServiceGrpc.HotelServiceBlockingStub hotelBlockingStub;

    @Override
    public Mono<Boolean> checkRoomAvailable(Integer hotelId, Integer roomId) {
        return Mono.fromCallable(() -> {
                    var request = RoomAvailabilityRequest.newBuilder()
                            .setHotelId(hotelId)
                            .setRoomId(roomId)
                            .build();

                    var response = hotelBlockingStub.checkRoomAvailability(request);
                    return response.getAvailable();
                })
                .subscribeOn(Schedulers.boundedElastic()) // run blocking call off main thread
                .onErrorResume(throwable -> {
                    // Log the error here
                    if (throwable instanceof StatusRuntimeException statusEx) {
                        var code = statusEx.getStatus().getCode();
                        log.error("gRPC error while checking room availability: {}, code={}", statusEx.getMessage(), code);
                    } else {
                        log.error("Unexpected error while checking room availability", throwable);
                    }

                    // Return fallback value (false = unavailable)
                    return Mono.just(false);
                });
    }
}
