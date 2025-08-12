package io.reactive.reservation.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@EqualsAndHashCode
@Table(name = "reservation")
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
public class Reservation {

    @Id
    private UUID id;

    @Column("hotel_id")
    private Integer hotelId;

    @Column("room_id")
    private Integer roomId;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("status")
    private Status status;

    @Column("guest_id")
    private Integer guestId;

    @Builder
    public Reservation(Integer hotelId,
                       Integer roomId,
                       LocalDate startDate,
                       LocalDate endDate,
                       Integer guestId) {
        this.id = UUID.randomUUID();
        this.hotelId = requireNonNull(hotelId, "hotelId cannot be null");
        this.roomId = requireNonNull(roomId, "roomId cannot be null");
        this.startDate = requireNonNull(startDate, "startDate cannot be null");
        this.endDate = requireNonNull(endDate, "startDate cannot be null");
        this.guestId = requireNonNull(guestId, "guestId cannot be null");
        this.status = Status.PENDING;
    }

    public void markSucceed() {
        this.status = Status.SUCCEED;
    }

    public void markFailed() {
        this.status = Status.FAILED;
    }

    public void markCancelled() {
        this.status = Status.CANCELED;
    }

    public enum Status {
        PENDING, SUCCEED, FAILED, CANCELED
    }
}
