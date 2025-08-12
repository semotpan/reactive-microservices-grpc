package io.reactive.reservation;

import io.reactive.reservation.domain.Reservation;
import io.reactive.reservation.domain.Reservations;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Configuration
public class TestDataInitializer {

//    @Bean
//    public CommandLineRunner testReservationInserter(Reservations reservations) {
//        return args -> {
//            var reservation1 = Reservation.builder()
//                    .hotelId(1L)
//                    .roomId(101L)
//                    .startDate(LocalDate.of(2025, 7, 1))
//                    .endDate(LocalDate.of(2025, 7, 5))
//                    .guestId(1001L)
//                    .build();
//
//            var reservation2 = Reservation.builder()
//                    .hotelId(2L)
//                    .roomId(202L)
//                    .startDate(LocalDate.of(2025, 8, 10))
//                    .endDate(LocalDate.of(2025, 8, 12))
//                    .guestId(1002L)
//                    .build();
//
//            var reservation3 = Reservation.builder()
//                    .hotelId(3L)
//                    .roomId(303L)
//                    .startDate(LocalDate.of(2025, 9, 15))
//                    .endDate(LocalDate.of(2025, 9, 20))
//                    .guestId(1003L)
//                    .build();
//
//            Flux.just(reservation1, reservation2, reservation3)
//                    .flatMap(reservations::add)
//                    .doOnNext(saved -> System.out.println("Saved reservation: " + saved.getId()))
//                    .blockLast();
//
//            Flux.just(reservation1, reservation2, reservation3)
//                    .flatMap(reservation -> reservations.find(reservation.getId()))
//                    .doOnNext(found -> System.out.println("Found reservation: " + found))
//                    .blockLast();
//        };
//    }
}
