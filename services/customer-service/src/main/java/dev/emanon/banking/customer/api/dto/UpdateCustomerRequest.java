package dev.emanon.banking.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @Pattern(
                regexp = ".*\\S.*",
                message = "must not be blank"
        )
        @Size(max = 100)
        String firstName,

        @Pattern(
                regexp = ".*\\S.*",
                message = "must not be blank"
        )
        @Size(max = 100)
        String lastName,

        @Pattern(
                regexp = ".*\\S.*",
                message = "must not be blank"
        )
        @Email
        @Size(max = 254)
        String email,

        @Pattern(
                regexp = "^$|^\\+?[0-9 ()-]{7,32}$",
                message = "must be empty or contain a valid phone number"
        )
        String phone,

        @NotNull
        @PositiveOrZero
        Long version
) {
}