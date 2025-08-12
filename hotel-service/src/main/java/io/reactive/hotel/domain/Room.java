package io.reactive.hotel.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Table(name = "room")
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Room {

    @Id
    private Integer id;

    private final Instant creationTime;

    private String name;
    private Integer number;
    private Integer floor;
    private Boolean available;
    private Integer hotelId;
}
