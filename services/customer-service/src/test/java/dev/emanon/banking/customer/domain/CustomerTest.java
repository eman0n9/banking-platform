package dev.emanon.banking.customer.domain;

import dev.emanon.banking.customer.domain.exception.InvalidCustomerStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    private Customer createCustomer() {
        return new Customer(
                "Dara",
                "Example",
                "dara@example.com",
                "+420777123456"
        );
    }

    @Test
    void newCustomerShouldBeActive() {
        Customer customer = createCustomer();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    void activeCustomerShouldBeBlocked() {
        Customer customer = createCustomer();

        customer.block();

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    }

    @Test
    void blockedCustomerShouldBeActivated() {
        Customer customer = createCustomer();
        customer.block();

        customer.activate();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    void customerShouldBeClosed() {
        Customer customer = createCustomer();

        customer.close();

        assertEquals(CustomerStatus.CLOSED, customer.getStatus());
    }

    @Test
    void closedCustomerCannotBeActivated() {
        Customer customer = createCustomer();
        customer.close();

        InvalidCustomerStatusTransitionException exception =
                assertThrows(
                        InvalidCustomerStatusTransitionException.class,
                        customer::activate
                );

        assertEquals(
                "Cannot change customer status from CLOSED to ACTIVE",
                exception.getMessage()
        );
    }

    @Test
    void closedCustomerCannotBeBlocked() {
        Customer customer = createCustomer();
        customer.close();

        assertThrows(
                InvalidCustomerStatusTransitionException.class,
                customer::block
        );
    }

    @Test
    void blockingAlreadyBlockedCustomerShouldNotFail() {
        Customer customer = createCustomer();
        customer.block();

        assertDoesNotThrow(customer::block);
        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    }
}