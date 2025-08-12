package io.reactive.hotel.adapter.grpc;

import io.grpc.stub.StreamObserver;
import io.reactive.hotel.application.RoomQuery;
import io.reactive.hotel.grpc.HotelServiceGrpc;
import io.reactive.hotel.grpc.RoomAvailabilityRequest;
import io.reactive.hotel.grpc.RoomAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class GrpcHotelServiceHandler extends HotelServiceGrpc.HotelServiceImplBase {

    private final RoomQuery roomQuery;

    @Override
    public void checkRoomAvailability(RoomAvailabilityRequest request, StreamObserver<RoomAvailabilityResponse> observer) {
        log.info("Checking room availability for hotel {} and room {}", request.getHotelId(), request.getRoomId());

        roomQuery.isRoomAvailable(request.getHotelId(), request.getRoomId())
                .defaultIfEmpty(false)
                .subscribe(
                        available -> {
                            var response = RoomAvailabilityResponse.newBuilder()
                                    .setAvailable(available)
                                    .build();
                            observer.onNext(response);
                            observer.onCompleted();
                            log.info("Room availability for hotel {} and room {}: availability: {}", request.getHotelId(), request.getRoomId(), response.getAvailable());
                        },
                        error -> {
                            log.error("Error while checking room availability", error);
                            observer.onError(error);
                        }
                );
    }
}
