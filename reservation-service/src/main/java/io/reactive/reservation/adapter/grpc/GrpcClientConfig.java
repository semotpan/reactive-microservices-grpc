package io.reactive.reservation.adapter.grpc;

import io.reactive.hotel.grpc.HotelServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
//@ImportGrpcClients
public class GrpcClientConfig {

    @Bean
    public HotelServiceGrpc.HotelServiceBlockingStub hotelBlockingStub(GrpcChannelFactory factory) {
        return HotelServiceGrpc.newBlockingStub(factory.createChannel("hotel-service"));
    }
}
