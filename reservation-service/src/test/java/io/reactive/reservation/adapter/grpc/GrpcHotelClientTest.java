package io.reactive.reservation.adapter.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactive.hotel.grpc.HotelServiceGrpc;
import io.reactive.hotel.grpc.RoomAvailabilityRequest;
import io.reactive.hotel.grpc.RoomAvailabilityResponse;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.io.IOException;

@Tag("integration")
class GrpcHotelClientTest {

    private static final String SERVER_NAME = "test-grpc-server";

    private Server server;
    private ManagedChannel channel;

    private GrpcHotelClient client;

    @BeforeEach
    void setUp() throws IOException {
        // Create and start an in-process gRPC server with a fake implementation
        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(new FakeHotelService())
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Create real stub wired to in-process server
        var stub = HotelServiceGrpc.newBlockingStub(channel);
        client = new GrpcHotelClient(stub);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) channel.shutdownNow();
        if (server != null) server.shutdownNow();
    }

    @Test
    @DisplayName("Should return true when fake gRPC service reports room available")
    void shouldReturnTrueWhenRoomIsAvailable() {
        StepVerifier.create(client.checkRoomAvailable(1, 101))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when fake gRPC service reports room unavailable")
    void shouldReturnFalseWhenRoomIsUnavailable() {
        StepVerifier.create(client.checkRoomAvailable(1, 999))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when gRPC service throws exception")
    void shouldReturnFalseOnGrpcFailure() {
        StepVerifier.create(client.checkRoomAvailable(0, 0)) // invalid input triggers error
                .expectNext(false)
                .verifyComplete();
    }

    // Fake gRPC service implementation for integration test
    static class FakeHotelService extends HotelServiceGrpc.HotelServiceImplBase {
        @Override
        public void checkRoomAvailability(RoomAvailabilityRequest request, StreamObserver<RoomAvailabilityResponse> responseObserver) {
            if (request.getHotelId() == 0 && request.getRoomId() == 0) {
                responseObserver.onError(Status.INTERNAL.withDescription("Boom").asRuntimeException());
                return;
            }

            boolean available = request.getRoomId() != 999;
            RoomAvailabilityResponse response = RoomAvailabilityResponse.newBuilder()
                    .setAvailable(available)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
