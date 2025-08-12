package io.reactive.hotel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class HotelServiceApplicationTest {

    @Test
    void contextLoads() {
    }

}

