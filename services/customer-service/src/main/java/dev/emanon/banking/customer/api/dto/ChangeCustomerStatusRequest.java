package dev.emanon.banking.customer.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChangeCustomerStatusRequest(

        @NotNull
        @PositiveOrZero
        Long version
) {
}