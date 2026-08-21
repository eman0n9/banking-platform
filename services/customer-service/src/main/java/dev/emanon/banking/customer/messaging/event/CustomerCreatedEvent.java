package dev.emanon.banking.customer.messaging.event;

import dev.emanon.banking.customer.domain.Customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID customerId,
        String status
) {

    public static CustomerCreatedEvent from(Customer customer) {
        return new CustomerCreatedEvent(
                UUID.randomUUID(),
                "CustomerCreated",
                1,
                Instant.now(),
                customer.getId(),
                customer.getStatus().name()
        );
    }
}