package io.reactive.hotel.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

import static lombok.AccessLevel.PACKAGE;

@Table(name = "hotel")
@NoArgsConstructor(access = PACKAGE, force = true) // JPA compliant
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Hotel {

    @Id
    private Integer id;

    private final Instant creationTime;

    private String name;
    private String address;
    private String location;

}
