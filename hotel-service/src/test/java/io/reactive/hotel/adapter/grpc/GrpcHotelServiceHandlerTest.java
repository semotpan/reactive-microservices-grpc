package io.reactive.hotel.adapter.grpc;

import io.reactive.hotel.TestcontainersConfiguration;
import io.reactive.hotel.domain.Hotel;
import io.reactive.hotel.domain.Room;
import io.reactive.hotel.grpc.HotelServiceGrpc;
import io.reactive.hotel.grpc.RoomAvailabilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@Tag("integration")
@SpringBootTest(
        properties = {
                "spring.grpc.server.port=0",
                "spring.grpc.client.default-channel.address=0.0.0.0:${local.grpc.port}"
        },
        useMainMethod = ALWAYS)
@DirtiesContext
@Import(TestcontainersConfiguration.class)
class GrpcHotelServiceHandlerTest {

    @Autowired
    HotelServiceGrpc.HotelServiceBlockingStub stub;

    @Autowired
    R2dbcEntityTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        var deleteAll = Mono.when(
                jdbcTemplate.delete(Hotel.class).all(),
                jdbcTemplate.delete(Room.class).all()
        );

        var insertData = deleteAll.thenMany(
                Flux.concat(
                        jdbcTemplate.insert(Hotel.class).using(new Hotel(1, Instant.now(), "Bristol Central Park Hotel", "str. Puskin 32, 2012", "Chişinău")),
                        jdbcTemplate.insert(Room.class).using(new Room(1, Instant.now(), "Twin with view", 38, 5, true, 1))
                )
        );

        StepVerifier.create(insertData).expectNextCount(2).verifyComplete();
    }

    @Test
    void shouldReturnAvailable() {
        var request = RoomAvailabilityRequest.newBuilder()
                .setHotelId(1)
                .setRoomId(1)
                .build();

        var response = stub.checkRoomAvailability(request);

        assertTrue(response.getAvailable());
    }

    @Test
    void shouldReturnUnavailable() {
        var request = RoomAvailabilityRequest.newBuilder()
                .setHotelId(1)
                .setRoomId(2)
                .build();

        var response = stub.checkRoomAvailability(request);

        assertFalse(response.getAvailable());
    }
}
