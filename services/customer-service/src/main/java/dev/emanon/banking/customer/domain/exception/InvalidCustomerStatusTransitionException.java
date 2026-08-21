package dev.emanon.banking.customer.domain.exception;

import dev.emanon.banking.customer.domain.CustomerStatus;

public class InvalidCustomerStatusTransitionException
        extends RuntimeException {

    public InvalidCustomerStatusTransitionException(
            CustomerStatus currentStatus,
            CustomerStatus targetStatus
    ) {
        super(
                "Cannot change customer status from %s to %s"
                        .formatted(currentStatus, targetStatus)
        );
    }
}