package dev.emanon.banking.customer.application.exception;

import java.util.UUID;

public class CustomerVersionConflictException extends RuntimeException {

    public CustomerVersionConflictException(
            UUID customerId,
            long requestedVersion,
            long currentVersion
    ) {
        super(
                "Customer version conflict for id %s: requested=%d, current=%d"
                        .formatted(
                                customerId,
                                requestedVersion,
                                currentVersion
                        )
        );
    }
}