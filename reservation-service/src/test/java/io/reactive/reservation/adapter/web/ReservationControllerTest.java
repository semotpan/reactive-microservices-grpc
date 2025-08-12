package io.reactive.reservation.adapter.web;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactive.hotel.grpc.HotelServiceGrpc;
import io.reactive.hotel.grpc.RoomAvailabilityRequest;
import io.reactive.hotel.grpc.RoomAvailabilityResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfiguration.class)
@DirtiesContext
class ReservationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    private static Server grpcServer;

    @BeforeAll
    static void startGrpcServer() throws IOException {
        grpcServer = InProcessServerBuilder
                .forName("test-grpc")
                .directExecutor()
                .addService(new FakeHotelService())
                .build()
                .start();
    }

    @AfterAll
    static void stopGrpcServer() {
        grpcServer.shutdownNow();
    }

    @TestConfiguration
    static class OverrideGrpcStub {

        @Bean
        public ManagedChannel grpcChannel() {
            return InProcessChannelBuilder.forName("test-grpc")
                    .directExecutor()
                    .build();
        }

        @Bean
        public HotelServiceGrpc.HotelServiceBlockingStub hotelStub(ManagedChannel grpcChannel) {
            return HotelServiceGrpc.newBlockingStub(grpcChannel);
        }
    }

    @Test
    @DisplayName("POST /api/v1/reservations - should succeed when room is available")
    void shouldCreateReservationWhenRoomIsAvailable() {
        var payload = Map.of(
                "hotelId", 1,
                "roomId", 101,
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(2).toString(),
                "guestId", 123
        );

        webTestClient.post()
                .uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.guestId").isEqualTo(123);
    }

    // Fake gRPC service used by the in-process server
    static class FakeHotelService extends HotelServiceGrpc.HotelServiceImplBase {
        @Override
        public void checkRoomAvailability(RoomAvailabilityRequest request,
                                          StreamObserver<RoomAvailabilityResponse> responseObserver) {
            boolean available = request.getRoomId() == 101;
            var response = RoomAvailabilityResponse.newBuilder()
                    .setAvailable(available)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
