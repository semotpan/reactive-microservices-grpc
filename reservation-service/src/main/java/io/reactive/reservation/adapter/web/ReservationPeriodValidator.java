package io.reactive.reservation.adapter.web;

import io.reactive.reservation.adapter.web.ReservationController.CreateReservationResource;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReservationPeriodValidator implements ConstraintValidator<ValidReservationPeriod, CreateReservationResource> {

    @Override
    public boolean isValid(CreateReservationResource resource, ConstraintValidatorContext context) {
        if (resource == null || resource.startDate() == null || resource.endDate() == null) {
            return true; // Let @NotNull handle null cases
        }
        return !resource.startDate().isAfter(resource.endDate());
    }
}
