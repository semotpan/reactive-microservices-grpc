package io.reactive.reservation.adapter.web;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReservationPeriodValidator.class)
@Documented
public @interface ValidReservationPeriod {
    String message() default "Start date must be before or equal to end date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
